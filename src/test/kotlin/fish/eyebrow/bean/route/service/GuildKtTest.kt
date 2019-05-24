package fish.eyebrow.bean.route.service

import fish.eyebrow.bean.dao.Group
import fish.eyebrow.bean.table.Groups
import fish.eyebrow.bean.table.Messages
import fish.eyebrow.bean.util.setupTestEngineWithConfiguration
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
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

    @Test
    internal fun `should update table with new group when given an id that does not exist`() {
        with(engine) {
            handleRequest(HttpMethod.Post, "/service/guild/1").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)

            }

            transaction {
                val groups = Group.all().toList()

                assertThat(groups).hasSize(1)
                assertThat(groups[0].id.value).isEqualTo(1)
            }
        }
    }

    @Test
    internal fun `should retain the exact same information with given id does exist`() {
        transaction {
            Group.new { }
        }

        with(engine) {
            handleRequest(HttpMethod.Post, "/service/guild/1").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)

            }

            transaction {
                val groups = Group.all().toList()

                assertThat(groups).hasSize(1)
                assertThat(groups[0].id.value).isEqualTo(1)
            }
        }
    }

    @Test
    internal fun `should respond with a bad request when not given an id`() {
        with(engine) {
            handleRequest(HttpMethod.Post, "/service/guild/").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.BadRequest)
            }

            transaction {
                val groups = Group.all().toList()

                assertThat(groups).isEmpty()
            }
        }
    }

    @Test
    internal fun `should remove group from table if given an id that exists`() {
        transaction {
            Group.new { }
        }

        with(engine) {
            handleRequest(HttpMethod.Delete, "/service/guild/1").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.OK)

            }

            transaction {
                val groups = Group.all().toList()

                assertThat(groups).hasSize(0)
            }
        }
    }

    @Test
    internal fun `should respond with a 404 when no group is found with id`() {
        transaction {
            Group.new { }
        }

        with(engine) {
            handleRequest(HttpMethod.Delete, "/service/guild/2").apply {
                assertThat(response.status()).isEqualTo(HttpStatusCode.NotFound)

            }

            transaction {
                val groups = Group.all().toList()

                assertThat(groups).hasSize(1)
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