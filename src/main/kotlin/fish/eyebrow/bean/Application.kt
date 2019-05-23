package fish.eyebrow.bean

import fish.eyebrow.bean.route.api.version
import io.ktor.application.Application
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI

fun main(args: Array<String>) {
    embeddedServer(Netty, commandLineEnvironment(args)).start(true)
}

@KtorExperimentalAPI
fun Application.api() {
    routing {
        route("api") {
            version()
        }
    }
}