package fish.eyebrow.bean.dao

import fish.eyebrow.bean.table.Messages
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass

class Message(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Message>(Messages)

    var content by Messages.content
    var group by Group referencedOn Messages.group

    class Simple(message: Message) {
        val id: Int = message.id.value
        val content: String = message.content
        val group: Group.Simple? = Group.Simple(message.group)
    }
}