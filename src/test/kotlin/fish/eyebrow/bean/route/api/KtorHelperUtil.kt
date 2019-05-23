package fish.eyebrow.bean.route.api

import com.typesafe.config.ConfigFactory
import fish.eyebrow.bean.api
import fish.eyebrow.bean.table.Messages
import io.ktor.config.HoconApplicationConfig
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.createTestEnvironment
import io.ktor.util.KtorExperimentalAPI
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

@KtorExperimentalAPI
internal fun setupTestEngineWithConfiguration(path: String): TestApplicationEngine {
    val engine = TestApplicationEngine(createTestEnvironment {
        config = HoconApplicationConfig(ConfigFactory.load(path))
    })
    engine.start(true)
    engine.application.api()

    return engine
}