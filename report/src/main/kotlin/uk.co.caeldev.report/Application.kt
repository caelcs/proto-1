package uk.co.caeldev.report


import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.typesafe.config.ConfigFactory
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.config.HoconApplicationConfig
import io.ktor.features.*
import io.ktor.http.HttpStatusCode
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.routing.routing
import org.koin.dsl.module.module
import org.koin.log.Logger.SLF4JLogger
import org.koin.standalone.StandAloneContext.startKoin
import org.slf4j.event.Level

fun Application.main() {
    install(DefaultHeaders)
    install(ConditionalHeaders)
    install(Compression)
    startKoin(listOf(adminModule, commonModule, messagingModule), logger = SLF4JLogger())

    install(StatusPages) {
        exception<NotImplementedError> { call.respond(HttpStatusCode.NotImplemented) }
    }
    install(CallId) {
        generate(10)
    }
    install(CallLogging) {
        level = Level.TRACE
        callIdMdc("X-Request-ID")
        filter { call -> call.request.path().startsWith("/") }
    }

    StreamsProcessor().process()

    routing {
        admin()
    }
}

val commonModule = module {
    single {
        HoconApplicationConfig(ConfigFactory.load())
    }

    single {
        ObjectMapper().apply {
            registerKotlinModule()
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            dateFormat = StdDateFormat()
        }
    }
}

