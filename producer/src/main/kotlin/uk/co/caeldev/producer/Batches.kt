package uk.co.caeldev.producer

import io.ktor.application.call
import io.ktor.config.HoconApplicationConfig
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.delete
import io.ktor.routing.post
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
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger


val batchesModule = module {

    single { KafkaConfigAck() }

}

object Batch {
    val batchSize: AtomicInteger = AtomicInteger(0)
    val continueRunning: AtomicBoolean = AtomicBoolean(true)
    val messageCounter: AtomicInteger = AtomicInteger(0)

    fun setBatchSize(batchSize: Int) {
        this.batchSize.set(batchSize)
    }

    fun stopRunning() {
        continueRunning.set(false)
    }

    fun startRunning() {
        continueRunning.set(true)
    }

    fun resetMessageCounter() {
        this.messageCounter.set(0)
    }
}

fun Routing.batches() {
    post("/batches") {
        val request = call.receive<BatchRequest>()
        Batch.setBatchSize(request.numberOfMessages)
        Batch.startRunning()
        Batch.resetMessageCounter()
        ConsumerMessageProducer(request.numberOfMessages).process()

        call.respond(HttpStatusCode.OK, "Messages created")
    }

    delete("/batches") {
        Batch.stopRunning()
        call.respond(HttpStatusCode.OK, "Batch Process stopping")
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

class AckConsumerService: KoinComponent {

    private val config: KafkaConfigAck by inject()
    private val configYml: HoconApplicationConfig by inject()

    fun start() {
        val streamsBuilder = StreamsBuilder()

        val personJsonStream: KStream<String, String> = streamsBuilder
                .stream(configYml.property("ktor.kafka.ackConsumer.ackConsumerTopic").getString(), Consumed.with(Serdes.String(), Serdes.String()))

        personJsonStream.peek { _, _ ->
            val counterUpdated = Batch.messageCounter.addAndGet(1)
            if (Batch.continueRunning.get() && counterUpdated == Batch.batchSize.get()) {
                Batch.resetMessageCounter()
                ConsumerMessageProducer(Batch.batchSize.get()).process()
            }
        }

        val topology = streamsBuilder.build()

        val streams = KafkaStreams(topology, config.props)
        streams.start()
    }
}

data class BatchRequest(val numberOfMessages: Int)