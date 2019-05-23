package fish.eyebrow.bean.table

import org.jetbrains.exposed.dao.IntIdTable

object Messages : IntIdTable() {
    val content = text("content")
}