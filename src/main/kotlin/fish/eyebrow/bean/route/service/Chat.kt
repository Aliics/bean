package fish.eyebrow.bean.route.service

import com.google.gson.Gson
import fish.eyebrow.bean.dao.Group
import fish.eyebrow.bean.dao.Message
import fish.eyebrow.bean.table.Messages
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.delete
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
            var statusCode = HttpStatusCode.OK

            transaction {
                val groupId = sentMessage.group?.id ?: 0
                val sentGroup = Group.findById(groupId)

                when {
                    sentMessage.id > 0 -> Message[sentMessage.id].apply {
                        content = sentMessage.content
                        this.group = sentGroup ?: this.group
                    }
                    sentMessage.id == 0 && sentGroup != null -> Message.new {
                        content = sentMessage.content
                        this.group = sentGroup
                    }
                    else -> statusCode = HttpStatusCode.BadRequest
                }
                commit()
            }
            call.respond(statusCode)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest)
        }
    }
    delete("$PATH/{id}") {
        val id = call.parameters["id"]!!.toInt()

        try {
            transaction {
                Message.findById(id)!!.delete()
                commit()
            }

            call.respond(HttpStatusCode.OK)
        } catch (e: KotlinNullPointerException) {
            call.respond(HttpStatusCode.NotFound)
        }
    }
}