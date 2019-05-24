package fish.eyebrow.bean

import fish.eyebrow.bean.route.service.chat
import fish.eyebrow.bean.route.service.guild
import fish.eyebrow.bean.route.service.version
import io.ktor.application.Application
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import org.jetbrains.exposed.sql.Database

private const val SERVICE_PATH = "service"

fun main(args: Array<String>) {
    embeddedServer(Netty, commandLineEnvironment(args)).start(true)
}

@KtorExperimentalAPI
fun Application.service() {
    configureDatabase()

    routing {
        route(SERVICE_PATH) {
            version()
            chat()
            guild()
        }
    }
}

@KtorExperimentalAPI
private fun Application.configureDatabase() {
    if (environment.config.propertyOrNull("ktor.application.db") != null) {
        Database.connect(
            url = environment.config.propertyOrNull("ktor.application.db.url")?.getString() ?: "",
            driver = environment.config.propertyOrNull("ktor.application.db.driver")?.getString() ?: "",
            user = environment.config.propertyOrNull("ktor.application.db.user")?.getString() ?: "",
            password = environment.config.propertyOrNull("ktor.application.db.password")?.getString() ?: ""
        )
    }
}