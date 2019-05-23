package fish.eyebrow.bean.util

import com.typesafe.config.ConfigFactory
import fish.eyebrow.bean.api
import io.ktor.config.HoconApplicationConfig
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.createTestEnvironment
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
internal fun setupTestEngineWithConfiguration(path: String): TestApplicationEngine {
    val engine = TestApplicationEngine(createTestEnvironment {
        config = HoconApplicationConfig(ConfigFactory.load(path))
    })
    engine.start(true)
    engine.application.api()

    return engine
}