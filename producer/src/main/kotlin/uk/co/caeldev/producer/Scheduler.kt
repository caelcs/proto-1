package uk.co.caeldev.producer

import io.ktor.application.call
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post
import org.koin.dsl.module.module
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import org.quartz.Job
import org.quartz.JobBuilder.newJob
import org.quartz.JobDetail
import org.quartz.JobExecutionContext
import org.quartz.Scheduler
import org.quartz.TriggerBuilder.newTrigger
import org.quartz.impl.StdSchedulerFactory
import uk.org.fyodor.generators.RDG.integer
import uk.org.fyodor.generators.RDG.string


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

    fun scheduleNow(job: JobDetail) {

        val trigger = newTrigger()
                .withIdentity("PersonTrigger", "Persona")
                .startNow()
                .build()

        scheduler.scheduleJob(job, trigger)
    }
}

fun Routing.scheduler() {

    val scheduler = SchedulerManager()

    post("/jobs") {
        val payload = call.receive<JobRequest>()
        val job = newJob(PersonJob::class.java)
                .withIdentity("PersonJob-${payload.name}-${integer().next()}", "Persona")
                .usingJobData("numberOfRecords", payload.numberOfRecords)
                .build()

        scheduler.scheduleNow(job = job)
        call.respond(OK, "Person created")
    }
}

data class JobRequest(val name: String, val numberOfRecords: Int)

class PersonJob: Job {

    private val messageService: MessagingService<Person> = MessagingService()

    constructor(){}

    override fun execute(context: JobExecutionContext?) {
        val dataMap = context?.jobDetail?.jobDataMap

        val numberOfRecords = if (dataMap?.containsKey("numberOfRecords") == true) dataMap?.getInt("numberOfRecords") else 0

        repeat(numberOfRecords) {
            messageService.createMessage(string().next(), Person(string().next(), string().next()))
        }
    }
}