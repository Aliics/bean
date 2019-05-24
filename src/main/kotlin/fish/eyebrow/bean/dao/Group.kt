package fish.eyebrow.bean.dao

import fish.eyebrow.bean.table.Groups
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass

class Group(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Group>(Groups)

    class Simple(group: Group) {
        val id = group.id.value
    }
}