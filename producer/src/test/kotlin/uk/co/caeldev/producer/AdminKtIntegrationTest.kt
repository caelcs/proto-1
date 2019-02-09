package uk.co.caeldev.producer

import io.ktor.application.Application
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.koin.test.AutoCloseKoinTest

internal class AdminKtIntegrationTest: AutoCloseKoinTest() {

    @Test
    fun `health check endpoint should return success`(): Unit = withTestApplication(Application::main) {
        with(handleRequest(HttpMethod.Get, "/health")) {
            assertThat(response.content).isEqualTo("OK")
            assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
        }
    }

    @Test
    fun `metrics endpoint should return success`(): Unit = withTestApplication(Application::main) {
        with(handleRequest(HttpMethod.Get, "/metrics")) {
            assertThat(response.content).isNotBlank()
            assertThat(response.status()).isEqualTo(HttpStatusCode.OK)
        }
    }
}