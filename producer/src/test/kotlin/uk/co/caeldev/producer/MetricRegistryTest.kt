package uk.co.caeldev.producer

import io.micrometer.core.instrument.binder.MeterBinder
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class MetricRegistryTest {

    @Test
    fun `should construct and bind all meters successfully`() {
        //Given
        val meterBinder = mockk<MeterBinder>(relaxed = true)

        //When
        MetricRegistry(listOf(meterBinder))

        //Then
        verify { meterBinder.bindTo(any()) }
    }

    @Test
    fun `should get all metrics available provided by meter binder`() {
        //Given
        val meterBinder = ClassLoaderMetrics()

        //When
        val metrics = MetricRegistry(listOf(meterBinder)).getMetrics()

        //Then
        assertThat(metrics).isNotBlank()
    }
}