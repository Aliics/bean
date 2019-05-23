package fish.eyebrow.bean.route.api

import io.ktor.application.application
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.util.KtorExperimentalAPI

const val PATH = "/version"

@KtorExperimentalAPI
fun Route.version() {
    get(PATH) {
        val conf = application.environment.config
        val version = conf.propertyOrNull("ktor.application.version")?.getString()
            ?: call.response.status(HttpStatusCode.NoContent)

        if (version !is Unit) {
            call.respond(version)
        }
    }
}