package uk.co.caeldev.producer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ser.std.StringSerializer
import io.ktor.config.HoconApplicationConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.koin.core.parameter.parametersOf
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

    init {
        val props = Properties()
        props["bootstrap.servers"] = config.property("ktor.kafka.brokers").getString()
        props["key.serializer"] = StringSerializer::class.java
        props["value.serializer"] = StringSerializer::class.java
        producer = KafkaProducer(props)
    }

    fun createMessage(topic: String, key: String, payload: T): Future<RecordMetadata> {
        return producer.send(ProducerRecord(topic, key, objectMapper.writeValueAsString(payload)))
    }
}