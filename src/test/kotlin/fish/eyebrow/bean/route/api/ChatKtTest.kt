package fish.eyebrow.bean.route.api

import fish.eyebrow.bean.table.Messages
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.util.KtorExperimentalAPI
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

@KtorExperimentalAPI
internal class ChatKtTest {
    @Test
    internal fun `should respond with the data in messages table`() {
        val engine = setupTestEngineWithConfiguration("configuration-with-db-setting.conf")

        transaction {
            SchemaUtils.create(Messages)
            Messages.insert {
                it[content] = "Hello, World!"
            }
            Messages.insert {
                it[content] = "Hello, Person!"
            }
        }

        with(engine) {
            handleRequest(HttpMethod.Get, "/api/chat").apply {
                val expected = """[{"id":1,"content":"Hello, World!"},{"id":2,"content":"Hello, Person!"}]"""
                assertThat(response.content).isEqualTo(expected)
            }
        }
    }

    @Test
    internal fun `should respond with empty when data in message table contain nothing`() {
        val engine = setupTestEngineWithConfiguration("configuration-with-db-setting.conf")

        transaction {
            SchemaUtils.create(Messages)
        }

        with(engine) {
            handleRequest(HttpMethod.Get, "/api/chat").apply {
                assertThat(response.content).isEqualTo("[]")
            }
        }
    }

    @Test
    internal fun `should fetch only specifically queries message when given id`() {
        val engine = setupTestEngineWithConfiguration("configuration-with-db-setting.conf")

        transaction {
            SchemaUtils.create(Messages)
            Messages.insert {
                it[content] = "Hello, World!"
            }
            Messages.insert {
                it[content] = "Hello, Person!"
            }
        }

        with(engine) {
            handleRequest(HttpMethod.Get, "/api/chat/2").apply {
                val expected = """[{"id":2,"content":"Hello, Person!"}]"""
                assertThat(response.content).isEqualTo(expected)
            }
        }
    }

    @AfterEach
    internal fun tearDown() {
        transaction {
            SchemaUtils.drop(Messages)
        }
    }
}