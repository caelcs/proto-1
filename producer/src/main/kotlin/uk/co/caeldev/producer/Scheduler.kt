package uk.co.caeldev.producer

import io.ktor.application.call
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import org.koin.dsl.module.module
import org.koin.ktor.ext.inject
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import org.quartz.Scheduler
import org.quartz.impl.StdSchedulerFactory

val schedulerModule = module {
    single {
        StdSchedulerFactory().scheduler
    }
}

class SchedulerManager: KoinComponent {

    private val scheduler: Scheduler by inject()

    fun start() {
        scheduler.start()
    }
}

fun Routing.scheduler() {

    val messagingService: MessagingService<Person> by inject("messagingServicePerson")

    post("/messages") {
        val payload = call.receive<Person>()
        val key = "${payload.name}:${payload.lastName}"
        messagingService.createMessage("testPerson", key, payload)
        call.respond(OK, "Person created")
    }
}

data class Person(val name: String, val lastName: String)