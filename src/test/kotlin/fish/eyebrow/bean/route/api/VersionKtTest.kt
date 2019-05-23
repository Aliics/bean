package fish.eyebrow.bean.route.api

import fish.eyebrow.bean.util.setupTestEngineWithConfiguration
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.util.KtorExperimentalAPI
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@KtorExperimentalAPI
internal class VersionKtTest {
    @Test
    internal fun `should respond with application version from config`() {
        val engine = setupTestEngineWithConfiguration("config-with-version.conf")
        with(engine) {
            handleRequest(HttpMethod.Get, "/api/version").apply {
                assertThat(response.content).isEqualTo("1.0.0-TEST")
            }
        }
    }

    @Test
    internal fun `should respond with 204 when config does not contain version`() {
        val engine = setupTestEngineWithConfiguration("config-without-version.conf")
        with(engine) {
            handleRequest(HttpMethod.Get, "/api/version").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NoContent)
            }
        }
    }

    @Test
    internal fun `should respond with 204 when config does not exist`() {
        val engine = setupTestEngineWithConfiguration("")
        with(engine) {
            handleRequest(HttpMethod.Get, "/api/version").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NoContent)
            }
        }
    }
}