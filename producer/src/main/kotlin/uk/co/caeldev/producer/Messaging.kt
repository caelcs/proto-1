package uk.co.caeldev.producer

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.application.call
import io.ktor.config.HoconApplicationConfig
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
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
import uk.org.fyodor.generators.RDG.string
import java.util.*
import java.util.concurrent.CompletableFuture.supplyAsync
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Supplier
import java.util.stream.IntStream
import kotlin.collections.set

val messagingModule = module {

    single("messagingServicePerson") {
        MessagingService<Person>()
    }

    single { KafkaConfig() }

    single {(props: Properties) ->
        KafkaProducer<String, String>(props)
    }

    single { KafkaConfigAck() }

}

class KafkaConfig: KoinComponent {
    private val config: HoconApplicationConfig by inject()
    val props = Properties()

    init {
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = config.property("ktor.kafka.brokers").getString()
        props[ProducerConfig.CLIENT_ID_CONFIG] = config.property("ktor.kafka.clientId").getString()
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
    }
}

class KafkaConfigAck: KoinComponent {
    private val config: HoconApplicationConfig by inject()
    val props = Properties()

    init {
        props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = config.property("ktor.kafka.brokers").getString()
        props[StreamsConfig.APPLICATION_ID_CONFIG] = config.property("ktor.kafka.ackConsumer.clientId").getString()
    }
}

class MessagingService<T>: KoinComponent {

    private val objectMapper: ObjectMapper by inject()
    private val config: KafkaConfig by inject()
    private val configYml: HoconApplicationConfig by inject()
    private val producer: KafkaProducer<String, String> by inject { parametersOf(config.props) }

    fun createMessage(key: String, payload: T): Future<RecordMetadata> {
        return producer.send(ProducerRecord(configYml.property("ktor.kafka.consumerTopic").getString(), key, objectMapper.writeValueAsString(payload)))
    }
}

object Batch {
    val batchSize: AtomicInteger = AtomicInteger(0)

    fun setBatchSize(batchSize: Int) {
        this.batchSize.set(batchSize)
    }
}

fun Routing.messaging() {
    post("/messages/{messages}") {
        val numberOfMessages = call.parameters["messages"]!!.toInt()
        Batch.setBatchSize(numberOfMessages)
        supplyAsync(Supplier {ConsumerMessageProducer(numberOfMessages).process()})
        call.respond(HttpStatusCode.OK, "Messages created")
    }
}

class ConsumerMessageProducer(private val numberOfMessages: Int): KoinComponent {

    private val messagingService: MessagingService<Person> by inject()
    private val metricRegistry: MetricRegistry by inject()

    fun process() {
        IntStream.range(0, numberOfMessages).parallel().forEach {
            val person = Person(string().next(), string().next())
            messagingService.createMessage(UUID.randomUUID().toString(), person)
        }
        metricRegistry.countBatch()
    }

}

class AckConsumerService: KoinComponent {

    private val config: KafkaConfigAck by inject()
    private val configYml: HoconApplicationConfig by inject()

    fun start() {
        val streamsBuilder = StreamsBuilder()

        val counter = AtomicInteger(0)

        val personJsonStream: KStream<String, String> = streamsBuilder
                .stream(configYml.property("ktor.kafka.ackConsumer.ackConsumerTopic").getString(), Consumed.with(Serdes.String(), Serdes.String()))

        personJsonStream.peek { _, _ ->
            val counterUpdated = counter.addAndGet(1)
            if (counterUpdated == Batch.batchSize.get()) {
                counter.set(0)
                supplyAsync(Supplier {ConsumerMessageProducer(Batch.batchSize.get()).process()})
            }
        }

        val topology = streamsBuilder.build()

        val streams = KafkaStreams(topology, config.props)
        streams.start()
    }
}

data class Person(val name: String, val lastName: String)