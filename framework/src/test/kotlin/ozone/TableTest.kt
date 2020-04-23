package dev.alpas.tests.ozone

import dev.alpas.exceptions.NotFoundHttpException
import dev.alpas.http.HttpCall
import dev.alpas.ozone.*
import dev.alpas.tests.BaseTest
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.entity.findAll
import me.liuwj.ktorm.entity.findList
import me.liuwj.ktorm.support.sqlite.SQLiteDialect
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import java.sql.Connection
import java.sql.DriverManager

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class OzoneTableTest : BaseTest() {
    lateinit var connection: Connection

    @BeforeEach
    fun beforeEach() {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:")
        Database.connect(logger = null, dialect = SQLiteDialect()) {
            object : Connection by connection {
                override fun close() {
                }
            }
        }
    }

    @AfterEach
    fun afterEach() {
        connection.close()
    }

    @Test
    fun `findOrCreate returns an existing entity if found`() {
        execSqlScript(TestTable.createSql)

        from(TestObjectFactory)
        TestTable().findOrCreate(mapOf("first_name" to "Default Name"))
        val all = TestTable().findAll()
        assertEquals(1, all.size)
    }

    @Test
    fun `findOrCreate does not override properties if found`() {
        execSqlScript(TestTable.createSql)

        from(TestObjectFactory)
        val oldEntity =
            TestTable().findOrCreate(mapOf("first_name" to "Default Name"), mapOf("first_name" to "New Name"))
        val all = TestTable().findAll()
        assertEquals(1, all.size)

        assertEquals("Default Name", oldEntity.firstName)
    }

    @Test
    fun `findOrCreate creates a new entity if not found`() {
        execSqlScript(TestTable.createSql)

        from(TestObjectFactory)
        val newEntity =
            TestTable().findOrCreate(mapOf("first_name" to "Does Not Exist"), mapOf("email" to "test@example.com"))
        assertEquals(2, TestTable().findAll().size)

        assertEquals("Does Not Exist", newEntity.firstName)
        assertEquals("test@example.com", newEntity.email)

        TestTable().findOrCreate(mapOf("email" to "test@example.com"))
        assertEquals(2, TestTable().findAll().size)
    }

    @Test
    fun `can find with multiple conditions`() {
        execSqlScript(TestTable.createSql)

        from(TestObjectFactory)
        val oldEntity =
            TestTable().findOrCreate(mapOf("first_name" to "Default Name", "email" to "Default Email"))
        val all = TestTable().findAll()
        assertEquals(1, all.size)

        assertEquals("Default Name", oldEntity.firstName)
    }

    @Test
    fun `can find using an assignment builder`() {
        execSqlScript(TestTable.createSql)

        from(TestObjectFactory)
        val newEntity =
            TestTable().findOrCreate(mapOf("first_name" to "Does Not Exist")) {
                it.email to "test@example.com"
            }
        assertEquals(2, TestTable().findAll().size)

        assertEquals("Does Not Exist", newEntity.firstName)
        assertEquals("test@example.com", newEntity.email)

        TestTable().findOrCreate(mapOf("email" to "test@example.com")) {
            it.email to "test@example.com"
        }
        assertEquals(2, TestTable().findAll().size)
    }

    @Test
    fun `can update using properties map`() {
        execSqlScript(TestTable.createSql)

        val entities = from(TestObjectFactory, 5)
        TestTable().update(mapOf("email" to "newemail@example.com")) {
            it.id eq entities.first().id
        }

        val defaultEmailEntities = TestTable().findMany(mapOf("email" to "Default Email"))
        assertEquals(4, defaultEmailEntities.size)

        val newEmailEntity = TestTable().findList { it.email eq "newemail@example.com" }
        assertEquals(1, newEmailEntity.size)
    }

    @Test
    fun `retrieve an entity based on call param`(@RelaxedMockK call: HttpCall) {
        execSqlScript(TestTable.createSql)
        val entities = from(TestObjectFactory, 5)

        every { call.param("id") } returns 2
        val entity = call.entity(TestTable())

        assertEquals(entity.firstName, entities[1].firstName)
    }

    @Test
    fun `retrieve an entity based on call param with a custom key`(@RelaxedMockK call: HttpCall) {
        execSqlScript(TestTable.createSql)
        val entities = from(TestObjectFactory, 5) {
            it.firstName to faker.name().firstName()
        }

        every { call.param("first_name") } returns entities[1].firstName
        val entity = call.entity(TestTable(), "first_name")

        assertEquals(entity.firstName, entities[1].firstName)
    }

    @Test
    fun `throws an exception if an entity based on call param is not found`(@RelaxedMockK call: HttpCall) {
        execSqlScript(TestTable.createSql)
        from(TestObjectFactory, 5)
        every { call.param("id") } returns 10
        Assertions.assertThrows(NotFoundHttpException::class.java) {
            call.entity(TestTable())
        }
    }

    @Test
    fun `throws an exception if param key is not found`(@RelaxedMockK call: HttpCall) {
        execSqlScript(TestTable.createSql)
        every { call.param("pid") } returns null
        Assertions.assertThrows(NotFoundHttpException::class.java) {
            call.entity(TestTable(), "pid")
        }
    }
}
