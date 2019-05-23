package fish.eyebrow.bean.route.api

import com.typesafe.config.ConfigFactory
import fish.eyebrow.bean.api
import io.ktor.config.HoconApplicationConfig
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.createTestEnvironment
import io.ktor.server.testing.handleRequest
import io.ktor.util.KtorExperimentalAPI
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@KtorExperimentalAPI
internal class VersionKtTest {
    private lateinit var engine: TestApplicationEngine

    @Test
    internal fun `should respond with application version from config`() {
        setupTestEngineWithConfiguration("config-with-version.conf")
        with(engine) {
            handleRequest(HttpMethod.Get, "/api/version").apply {
                assertThat(response.content).isEqualTo("1.0.0-TEST")
            }
        }
    }

    @Test
    internal fun `should respond with 204 when config does not contain version`() {
        setupTestEngineWithConfiguration("config-without-version.conf")
        with(engine) {
            handleRequest(HttpMethod.Get, "/api/version").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NoContent)
            }
        }
    }

    @Test
    internal fun `should respond with 204 when config does not exist`() {
        setupTestEngineWithConfiguration("")
        with(engine) {
            handleRequest(HttpMethod.Get, "/api/version").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NoContent)
            }
        }
    }

    private fun setupTestEngineWithConfiguration(path: String) {
        engine = TestApplicationEngine(createTestEnvironment {
            config = HoconApplicationConfig(ConfigFactory.load(path))
        })
        engine.start(true)
        engine.application.api()
    }
}