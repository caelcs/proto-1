package uk.co.caeldev.producer

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.common.serialization.StringSerializer
import io.ktor.config.HoconApplicationConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.koin.dsl.module.module
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.util.*
import java.util.concurrent.Future

val messagingModule = module {

    single("messagingServicePerson") {
        MessagingService<Person>()
    }

}

class MessagingService<T>: KoinComponent {

    private val config: HoconApplicationConfig by inject()
    private val objectMapper: ObjectMapper by inject()
    private val producer: KafkaProducer<String, String>

    constructor() {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = config.property("ktor.kafka.brokers").getString()
        props[ProducerConfig.CLIENT_ID_CONFIG] = "producer"
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        producer = KafkaProducer(props)
    }

    fun createMessage(topic: String, key: String, payload: T): Future<RecordMetadata> {
        return producer.send(ProducerRecord(topic, key, objectMapper.writeValueAsString(payload)))
    }
}