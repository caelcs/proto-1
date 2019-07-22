package uk.co.caeldev.report

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.config.HoconApplicationConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.dsl.module
import java.util.*

val messagingModule = module {

    single { KafkaConfig() }

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
    private val objectMapper: ObjectMapper by inject()

    fun process() {
        val streamsBuilder = StreamsBuilder()

        val personJsonStream: KStream<String, String> = streamsBuilder
                .stream(configYml.property("ktor.kafka.ackConsumer.ackConsumerTopic").getString(), Consumed.with(Serdes.String(), Serdes.String()))

        personJsonStream.peek { _, value ->
            val person = objectMapper.readValue(value, Person::class.java)
        }

        val topology = streamsBuilder.build()

        val streams = KafkaStreams(topology, config.props)
        streams.start()
    }
}

data class Person(val name: String, val lastName: String)
