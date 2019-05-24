package fish.eyebrow.bean.route.service

import com.google.gson.Gson
import fish.eyebrow.bean.dao.Group
import fish.eyebrow.bean.table.Groups
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import org.jetbrains.exposed.sql.transactions.transaction

private const val PATH = "/guild"


fun Route.guild() {
    get("$PATH/{id?}") {
        val id = call.parameters["id"]?.toInt()
        val result = transaction {
            if (id != null) {
                Group.find { Groups.id eq id }.map { Group.Simple(it) }
            } else {
                Group.all().map { Group.Simple(it) }
            }
        }
        val resultJson = Gson().toJson(result)

        call.respond(resultJson)
    }
    post("$PATH/{id?}") {
        val id = call.parameters["id"]?.toInt()

        if (id != null) {
            transaction {
                if (Group.findById(id) == null) {
                    Group.new(id) { }
                }
            }

            call.respond(HttpStatusCode.OK)
        } else {
            call.respond(HttpStatusCode.BadRequest)
        }
    }
    delete("$PATH/{id?}") {
        val id = call.parameters["id"]?.toInt()

        try {
            transaction {
                Group[id!!].delete()
            }

            call.respond(HttpStatusCode.OK)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.NotFound)
        }
    }
}