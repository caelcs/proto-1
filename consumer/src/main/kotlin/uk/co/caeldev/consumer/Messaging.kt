package uk.co.caeldev.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.config.HoconApplicationConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module.module
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import uk.org.fyodor.generators.RDG.longVal
import uk.org.fyodor.range.Range
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.Future

val messagingModule = module {

    single { KafkaConfig() }

    single { KafkaConfigProducer() }

    single { MessagingService<Person>() }

    single { AckProducer() }

}

class KafkaConfig: KoinComponent {
    private val config: HoconApplicationConfig by inject()
    val props = Properties()

    init {
        props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = config.property("ktor.kafka.brokers").getString()
        props[StreamsConfig.APPLICATION_ID_CONFIG] = config.property("ktor.kafka.applicationId").getString()
    }
}

class StreamsProcessor: KoinComponent {

    private val config: KafkaConfig by inject()
    private val configYml: HoconApplicationConfig by inject()
    private val metricRegistry: MetricRegistry by inject()
    private val executor: Executor = Executors.newFixedThreadPool(200)

    fun process() {
        val streamsBuilder = StreamsBuilder()

        val personJsonStream: KStream<String, String> = streamsBuilder
                .stream(configYml.property("ktor.kafka.consumerTopic").getString(), Consumed.with(Serdes.String(), Serdes.String()))

        personJsonStream.peek { key, value ->
            CompletableFuture.runAsync(sendRequest, executor)
        }

        val topology = streamsBuilder.build()

        val streams = KafkaStreams(topology, config.props)
        streams.start()
    }

    private val sendRequest = Runnable {
        Thread.sleep(longVal(Range.closed(500L, 10000L)).next())
        metricRegistry.countMessage()
    }
}

class KafkaConfigProducer: KoinComponent {
    private val config: HoconApplicationConfig by inject()
    val props = Properties()

    init {
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = config.property("ktor.kafka.brokers").getString()
        props[ProducerConfig.CLIENT_ID_CONFIG] = config.property("ktor.kafka.ackConsumer.clientId").getString()
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
    }
}

class MessagingService<T>: KoinComponent {

    private val objectMapper: ObjectMapper by inject()
    private val config: KafkaConfigProducer by inject()
    private val configYml: HoconApplicationConfig by inject()
    private val producer: KafkaProducer<String, String> by inject { parametersOf(config.props) }

    fun createMessage(key: String, payload: T): Future<RecordMetadata> {
        return producer.send(ProducerRecord(configYml.property("ktor.kafka.ackConsumer.ackConsumerTopic").getString(), key, objectMapper.writeValueAsString(payload)))
    }
}

class AckProducer: KoinComponent {

    private val messagingService: MessagingService<Person> by inject()

    fun produce(id: UUID, person: Person) {
        messagingService.createMessage(id.toString(), person)
    }

}

data class Person(val name: String, val lastName: String)
