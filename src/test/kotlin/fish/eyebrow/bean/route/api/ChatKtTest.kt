package fish.eyebrow.bean.route.api

import fish.eyebrow.bean.dao.Message
import fish.eyebrow.bean.table.Messages
import fish.eyebrow.bean.util.setupTestEngineWithConfiguration
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.util.KtorExperimentalAPI
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@KtorExperimentalAPI
internal class ChatKtTest {
    private lateinit var engine: TestApplicationEngine

    @BeforeEach
    internal fun setUp() {
        engine = setupTestEngineWithConfiguration("configuration-with-db-setting.conf")

        transaction {
            SchemaUtils.create(Messages)
        }
    }

    @Test
    internal fun `should respond with the data in messages table`() {
        transaction {
            Messages.insert {
                it[content] = "Hello, World!"
            }
            Messages.insert {
                it[content] = "Hello, Person!"
            }
        }

        with(engine) {
            handleRequest(HttpMethod.Get, "/service/chat").apply {
                val expected = """[{"id":1,"content":"Hello, World!"},{"id":2,"content":"Hello, Person!"}]"""
                assertThat(response.content).isEqualTo(expected)
            }
        }
    }

    @Test
    internal fun `should respond with empty when data in message table contain nothing`() {
        with(engine) {
            handleRequest(HttpMethod.Get, "/service/chat").apply {
                assertThat(response.content).isEqualTo("[]")
            }
        }
    }

    @Test
    internal fun `should fetch only specifically queries message when given id`() {
        transaction {
            Messages.insert {
                it[content] = "Hello, World!"
            }
            Messages.insert {
                it[content] = "Hello, Person!"
            }
        }

        with(engine) {
            handleRequest(HttpMethod.Get, "/service/chat/2").apply {
                val expected = """[{"id":2,"content":"Hello, Person!"}]"""
                assertThat(response.content).isEqualTo(expected)
            }
        }
    }

    @Test
    internal fun `should insert a message into messages when posting`() {
        with(engine) {
            handleRequest(HttpMethod.Post, "/service/chat") {
                setBody("""{"content":"I'm the postman!"}""")
            }
        }

        val result = transaction { Message.all().map { Message.Simple(it) } }

        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo(1)
        assertThat(result[0].content).isEqualTo("I'm the postman!")
    }

    @Test
    internal fun `should update already existing message when body contains an id`() {
        transaction {
            Message.new { content = "This should not be visible" }
        }

        with(engine) {
            handleRequest(HttpMethod.Post, "/service/chat") {
                setBody("""{"id":1,"content":"The final message!"}""")
            }
        }

        val result = transaction { Message.all().map { Message.Simple(it) } }

        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo(1)
        assertThat(result[0].content).isEqualTo("The final message!")
    }

    @Test
    internal fun `should respond with a bad request when there is no body content`() {
        with(engine) {
            handleRequest(HttpMethod.Post, "/service/chat").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
            }
        }
    }

    @Test
    internal fun `should respond with a bad request when body is malformed`() {
        with(engine) {
            handleRequest(HttpMethod.Post, "/service/chat") {
                setBody("foobar")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
            }
        }
    }

    @Test
    internal fun `should remove message from messages when requesting to delete with id`() {
        transaction {
            Message.new { content = "This should not be visible" }
        }

        with(engine) {
            handleRequest(HttpMethod.Delete, "/service/chat/1")
        }

        val result = transaction {
            Message.all().map { Message.Simple(it) }
        }

        assertThat(result).hasSize(0)
    }

    @Test
    internal fun `should not send request when no id is provided to deletion`() {
        transaction {
            Message.new { content = "This should not be visible" }
        }

        with(engine) {
            handleRequest(HttpMethod.Delete, "/service/chat").apply {
                assertThat(response.status()).isNull()
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