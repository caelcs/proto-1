package uk.co.caeldev.producer

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.application.call
import io.ktor.config.HoconApplicationConfig
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.serialization.StringSerializer
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module.module
import org.koin.ktor.ext.inject
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.util.*
import java.util.concurrent.Future
import kotlin.collections.set

val messagingModule = module {

    single("messagingServicePerson") {
        MessagingService<Person>()
    }

    single { KafkaConfig() }

    single {(props: Properties) ->
        KafkaProducer<String, String>(props)
    }
}

class KafkaConfig: KoinComponent {
    private val config: HoconApplicationConfig by inject()
    val props = Properties()

    init {
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = config.property("ktor.kafka.brokers").getString()
        props[ProducerConfig.CLIENT_ID_CONFIG] = config.property("ktor.kafka.brokers.clientId").getString()
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
    }
}

class MessagingService<T>: KoinComponent {

    private val objectMapper: ObjectMapper by inject()
    private val config: KafkaConfig by inject()
    private val producer: KafkaProducer<String, String> by inject { parametersOf(config.props) }

    fun createMessage(topic: String, key: String, payload: T): Future<RecordMetadata> {
        return producer.send(ProducerRecord(topic, key, objectMapper.writeValueAsString(payload)))
    }
}


fun Routing.messaging() {

    val messagingService: MessagingService<Person> by inject("messagingServicePerson")

    post("/messages") {
        val payload = call.receive<Person>()
        val key = "${payload.name}:${payload.lastName}"
        messagingService.createMessage("admintome-test", key, payload)
        call.respond(HttpStatusCode.OK, "Person created")
    }
}

data class Person(val name: String, val lastName: String)