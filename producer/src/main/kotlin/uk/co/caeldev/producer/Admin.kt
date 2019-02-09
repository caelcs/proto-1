package uk.co.caeldev.producer

import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.micrometer.core.instrument.binder.MeterBinder
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import org.koin.dsl.module.module
import org.koin.ktor.ext.inject

val adminModule = module {
    single {
        MetricRegistry(listOf(ClassLoaderMetrics(),
                JvmMemoryMetrics(), JvmThreadMetrics(), ProcessorMetrics()))
    }
}

fun Routing.admin() {

    val metricRegistry: MetricRegistry by inject()

    get("/health") {
        call.respondText("OK")
    }

    get("/metrics") {
        call.respondText(metricRegistry.getMetrics())
    }
}

class MetricRegistry(metrics: List<MeterBinder>) {

    private val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    init {
        metrics.forEach{
            it.bindTo(registry)
        }
    }

    fun getMetrics(): String = registry.scrape()
}