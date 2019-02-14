package uk.co.caeldev.consumer

import io.ktor.config.HoconApplicationConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.koin.dsl.module.module
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.time.Duration
import java.util.*

val messagingModule = module {

    single { KafkaConfig() }

}

class KafkaConfig: KoinComponent {
    private val config: HoconApplicationConfig by inject()
    val props = Properties()

    init {
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = config.property("ktor.kafka.brokers").getString()
        props[ConsumerConfig.GROUP_ID_CONFIG] = config.property("ktor.kafka.applicationId").getString()
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
    }
}

class StreamsProcessor: KoinComponent {

    private val config: KafkaConfig by inject()
    private val configYml: HoconApplicationConfig by inject()
    private val metricRegistry: MetricRegistry by inject()

    fun process() {

        Thread({
            val consumer = KafkaConsumer<String, String>(config.props)
            consumer.subscribe(listOf(configYml.property("ktor.kafka.proto1Topic").getString()))

            val running = true
            while (running) {
                val records = consumer.poll(Duration.ofMillis(100))
                for (record in records) {
                    metricRegistry.countMessage()
                }
            }

            consumer.close()
        }).start()

    }
}
