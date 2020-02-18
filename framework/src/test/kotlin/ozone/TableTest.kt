package dev.alpas.tests.ozone

import dev.alpas.ozone.findOrCreate
import dev.alpas.ozone.from
import dev.alpas.tests.BaseTest
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.entity.findAll
import me.liuwj.ktorm.support.sqlite.SQLiteDialect
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.sql.Connection
import java.sql.DriverManager

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
}
