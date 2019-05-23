package fish.eyebrow.bean.route.api

import com.google.gson.Gson
import fish.eyebrow.bean.dao.Message
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import org.jetbrains.exposed.sql.transactions.transaction

private const val PATH = "/chat"

fun Route.chat() {
    get(PATH) {
        val result= transaction { Message.all().map { Message.Simple(it) } }
        val resultJson = Gson().toJson(result)

        call.respond(resultJson)
    }
}