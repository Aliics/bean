package fish.eyebrow.bean.route.service

import fish.eyebrow.bean.dao.Group
import fish.eyebrow.bean.dao.Message
import fish.eyebrow.bean.table.Groups
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
            SchemaUtils.create(Groups, Messages)
            Group.new {}
        }
    }

    @Test
    internal fun `should respond with the data in messages table`() {
        transaction {
            val groupObject = Group[1]
            Message.new {
                content = "Hello, World!"
                group = groupObject
            }
            Message.new {
                content = "Hello, Person!"
                group = groupObject
            }
        }

        with(engine) {
            handleRequest(HttpMethod.Get, "/service/chat").apply {
                val expected = """[{"id":1,"content":"Hello, World!","group":{"id":1}},{"id":2,"content":"Hello, Person!","group":{"id":1}}]"""
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
            val groupObject = Group[1]
            Message.new {
                content = "Hello, World!"
                group = groupObject
            }
            Message.new {
                content = "Hello, Person!"
                group = groupObject
            }
        }

        with(engine) {
            handleRequest(HttpMethod.Get, "/service/chat/2").apply {
                val expected = """[{"id":2,"content":"Hello, Person!","group":{"id":1}}]"""
                assertThat(response.content).isEqualTo(expected)
            }
        }
    }

    @Test
    internal fun `should insert a message into messages when posting`() {
        with(engine) {
            handleRequest(HttpMethod.Post, "/service/chat") {
                setBody("""{"content":"I'm the postman!","group":{"id":1}}""")
            }
        }

        val result = transaction { Message.all().map { Message.Simple(it) } }

        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo(1)
        assertThat(result[0].content).isEqualTo("I'm the postman!")
    }

    @Test
    internal fun `should update already existing message when body contains an id and no group`() {
        transaction {
            val groupObject = Group[1]

            Message.new {
                content = "This should not be visible"
                group = groupObject
            }
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
    internal fun `should respond with a bad request when group is missing and not available`() {
        with(engine) {
            handleRequest(HttpMethod.Post, "/service/chat") {
                setBody("""{"id":1,"content":"The final message!"}""")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
            }
        }
    }

    @Test
    internal fun `should respond with a bad request when group and id are not given`() {
        with(engine) {
            handleRequest(HttpMethod.Post, "/service/chat") {
                setBody("""{"content":"The final message!"}""")
            }.apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
            }
        }
    }

    @Test
    internal fun `should remove message from messages when requesting to delete with id`() {
        transaction {
            val groupObject = Group[1]
            Message.new {
                content = "This should not be visible"
                group = groupObject
            }
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
    internal fun `should return 404 when requested message to delete does not exist`() {
        transaction {
            val groupObject = Group[1]
            Message.new {
                content = "This should be visible"
                group = groupObject
            }
        }

        with(engine) {
            handleRequest(HttpMethod.Delete, "/service/chat/2").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
            }
        }

        val result = transaction {
            Message.all().map { Message.Simple(it) }
        }

        assertThat(result).hasSize(1)
    }

    @Test
    internal fun `should not send request when no id is provided to deletion`() {
        transaction {
            val groupObject = Group[1]
            Message.new {
                content = "This should not be visible"
                group = groupObject
            }
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
            SchemaUtils.drop(Groups, Messages)
        }
    }
}