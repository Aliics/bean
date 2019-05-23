package fish.eyebrow.bean

import fish.eyebrow.bean.route.api.chat
import fish.eyebrow.bean.route.api.version
import io.ktor.application.Application
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import org.jetbrains.exposed.sql.Database

fun main(args: Array<String>) {
    embeddedServer(Netty, commandLineEnvironment(args)).start(true)
}

@KtorExperimentalAPI
fun Application.api() {
    if (environment.config.propertyOrNull("ktor.application.db") != null) {
        Database.connect(
            url = environment.config.propertyOrNull("ktor.application.db.url")?.getString() ?: "",
            driver = environment.config.propertyOrNull("ktor.application.db.driver")?.getString() ?: "",
            user = environment.config.propertyOrNull("ktor.application.db.user")?.getString() ?: "",
            password = environment.config.propertyOrNull("ktor.application.db.password")?.getString() ?: ""
        )
    }

    routing {
        route("api") {
            version()
            chat()
        }
    }
}