package uk.co.caeldev.producer


import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.http.HttpStatusCode
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.routing.routing
import org.koin.log.Logger.SLF4JLogger
import org.koin.standalone.StandAloneContext.startKoin
import org.slf4j.event.Level

fun Application.main() {
    install(DefaultHeaders)
    install(ConditionalHeaders)
    install(Compression)
    startKoin(listOf(adminModule), logger = SLF4JLogger())

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

    routing {
        admin()
    }
}

