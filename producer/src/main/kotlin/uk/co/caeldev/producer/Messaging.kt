package uk.co.caeldev.producer

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.config.HoconApplicationConfig
import kotlinx.coroutines.*
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.serialization.StringSerializer
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module.module
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import uk.org.fyodor.generators.RDG.string
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
        props[ProducerConfig.CLIENT_ID_CONFIG] = config.property("ktor.kafka.clientId").getString()
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
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

class ConsumerMessageProducer(private val numberOfMessages: Int): KoinComponent {

    private val messagingService: MessagingService<Person> by inject()
    private val metricRegistry: MetricRegistry by inject()

    fun process() {
        GlobalScope.async {
            coroutineScope {
                repeat(numberOfMessages) {
                    async {
                        val person = Person(string().next(), string().next())
                        messagingService.createMessage(UUID.randomUUID().toString(), person)
                    }
                }
            }
            metricRegistry.countBatch()
        }
    }

}

data class Person(val name: String, val lastName: String)