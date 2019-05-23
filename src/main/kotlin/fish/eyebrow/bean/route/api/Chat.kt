package fish.eyebrow.bean.route.api

import com.google.gson.Gson
import fish.eyebrow.bean.dao.Message
import fish.eyebrow.bean.table.Messages
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import org.jetbrains.exposed.sql.transactions.transaction

private const val PATH = "/chat"

fun Route.chat() {
    get("$PATH/{id?}") {
        val id = call.parameters["id"]?.toInt()
        val result = transaction {
            if (id != null) {
                Message.find { Messages.id eq id }.map { Message.Simple(it) }
            } else {
                Message.all().map { Message.Simple(it) }
            }
        }
        val resultJson = Gson().toJson(result)

        call.respond(resultJson)
    }
    post(PATH) {
        try {
            val sentMessage = Gson().fromJson(call.receiveText(), Message.Simple::class.java)
            transaction {
                if (sentMessage.id > 0) {
                    Message[sentMessage.id].content = sentMessage.content
                } else {
                    Message.new { content = sentMessage.content }
                }
                commit()
            }
            call.respond(HttpStatusCode.OK)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest)
        }
    }
}