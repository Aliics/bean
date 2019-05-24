package fish.eyebrow.bean.route.service

import fish.eyebrow.bean.dao.Group
import fish.eyebrow.bean.table.Groups
import fish.eyebrow.bean.table.Messages
import fish.eyebrow.bean.util.setupTestEngineWithConfiguration
import io.ktor.http.HttpMethod
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.util.KtorExperimentalAPI
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@KtorExperimentalAPI
internal class GuildKtTest {
    @BeforeEach
    internal fun setUp() {
        engine = setupTestEngineWithConfiguration("configuration-with-db-setting.conf")

        transaction {
            SchemaUtils.create(Groups, Messages)
        }
    }

    private lateinit var engine: TestApplicationEngine

    @Test
    internal fun `should obtain all groups without any id specified and table is populated`() {
        transaction {
            Group.new { }
            Group.new { }
        }

        with(engine) {
            handleRequest(HttpMethod.Get, "/service/guild").apply {
                assertThat(response.content).isEqualTo("""[{"id":1},{"id":2}]""")
            }
        }
    }

    @Test
    internal fun `should obtain an empty list when no groups are in table`() {
        with(engine) {
            handleRequest(HttpMethod.Get, "/service/guild").apply {
                assertThat(response.content).isEqualTo("""[]""")
            }
        }
    }

    @Test
    internal fun `should obtain only one specific group when querying with an id`() {
        transaction {
            Group.new { }
            Group.new { }
        }

        with(engine) {
            handleRequest(HttpMethod.Get, "/service/guild/2").apply {
                assertThat(response.content).isEqualTo("""[{"id":2}]""")
            }
        }
    }

    @Test
    internal fun `should obtain an empty list when id given does not exist`() {
        with(engine) {
            handleRequest(HttpMethod.Get, "/service/guild/2").apply {
                assertThat(response.content).isEqualTo("""[]""")
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