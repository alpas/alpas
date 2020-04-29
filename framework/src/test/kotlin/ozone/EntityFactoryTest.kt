package dev.alpas.tests.ozone

import dev.alpas.ozone.*
import dev.alpas.tests.BaseTest
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.entity.findAll
import me.liuwj.ktorm.entity.findById
import me.liuwj.ktorm.logging.NoOpLogger
import me.liuwj.ktorm.schema.int
import me.liuwj.ktorm.support.sqlite.SQLiteDialect
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.sql.Connection
import java.sql.DriverManager

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EntityFactoryTest : BaseTest() {
    lateinit var connection: Connection

    @BeforeEach
    fun beforeEach() {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:")
        Database.connect(logger = NoOpLogger, dialect = SQLiteDialect()) {
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
    fun `can override entity's properties`() {
        TestObjectFactory.build().apply {
            assertEquals("Default Name", firstName)
            assertEquals("Default Email", email)
        }

        TestObjectFactory.build(mapOf("first_name" to "New Name")).apply {
            assertEquals("New Name", firstName)
            assertEquals("Default Email", email)
        }
    }

    @Test
    fun `entity can persist in the database`() {
        execSqlScript(TestTable.createSql)

        val entity = from(TestObjectFactory)
        val all = TestTable().findAll()
        assertEquals(1, all.size)
        val dbEntity = TestTable().findById(entity.id)
        assertNotNull(dbEntity)
    }

    @Test
    fun `can transform a value before persisting in the database`() {
        execSqlScript(TestTable.createSql)

        val entity = from(TransformingTestObjectFactory)
        val all = TestTable().findAll()
        assertEquals(1, all.size)
        val dbEntity = TestTable().findById(entity.id)
        assertNotNull(dbEntity)

        assertNotEquals("Default Name", dbEntity?.firstName)
        assertEquals("DEFAULT NAME", dbEntity?.firstName)
        assertEquals("Default Email", dbEntity?.email)
    }

    @Test
    fun `can override persistent entity's properties`() {
        execSqlScript(TestTable.createSql)

        val entity = from(TestObjectFactory, "first_name" to "Test Name")
        val all = TestTable().findAll()
        assertEquals(1, all.size)
        val dbEntity = TestTable().findById(entity.id)
        assertNotNull(dbEntity)
        assertEquals("Test Name", dbEntity?.firstName)
    }

    @Test
    fun `can override persistent entity's properties with an assignment builder`() {
        execSqlScript(TestTable.createSql)

        val entity = from(TestObjectFactory) {
            it.firstName to "Test Name"
        }
        val all = TestTable().findAll()
        assertEquals(1, all.size)
        val dbEntity = TestTable().findById(entity.id)
        assertNotNull(dbEntity)
        assertEquals("Test Name", dbEntity?.firstName)
    }

    @Test
    fun `extra properties will be ignored`() {
        execSqlScript(TestTable.createSql)

        val entity = from(TestObjectFactory, "first_name" to "Test Name", "address" to "Random Street")
        val all = TestTable().findAll()
        assertEquals(1, all.size)
        val dbEntity = TestTable().findById(entity.id)
        assertNotNull(dbEntity)
        assertEquals("Test Name", dbEntity?.firstName)
        assertFalse(dbEntity?.properties?.containsKey("address") ?: true)
    }

    @Test
    fun `entity can be persisted after it is built`() {
        execSqlScript(TestTable.createSql)

        val entity = TestObjectFactory.build()
        TestTable().findAll().apply {
            assertEquals(0, size)
        }

        TestObjectFactory.save(entity)
        TestTable().findAll().apply {
            assertEquals(1, size)
        }
    }

    @Test
    fun `extra properties is removed before persisting`() {
        execSqlScript(TestTable.createSql)

        val entity = TestObjectFactory.build(mapOf("extra_prop" to "extraaaa"))
        assertEquals("extraaaa", entity.properties["extra_prop"])

        TestTable().findAll().apply {
            assertEquals(0, size)
        }

        TestObjectFactory.save(entity)
        TestTable().findAll().apply {
            assertEquals(1, size)
        }
    }

    @Test
    fun `save and refresh fetches a new copy`() {
        execSqlScript(TestTable.createSql)

        val entity = TestObjectFactory.build()
        TestTable().findAll().apply {
            assertEquals(0, size)
        }

        val newEntity = TestObjectFactory.saveAndRefresh(entity)
        TestTable().findAll().apply {
            assertEquals(1, size)
        }
        assertNotEquals(newEntity, entity)
    }

    @Test
    fun `entity can create persist in the database`() {
        execSqlScript(TestTable.createSql)

        from(TestObjectFactory, 5, "first_name" to "Same Name")
        val all = TestTable().findAll()
        assertEquals(5, all.size)

        assertEquals(all.random().firstName, "Same Name")
    }

    @Test
    fun `creating many entities returns fresh copies`() {
        execSqlScript(TestTable.createSql)

        val entities = from(TestObjectFactory, 10, "first_name" to "Same Name")
        assertEquals(10, entities.size)

        assertNotNull(entities.random().id)
        assertEquals(entities.random().firstName, "Same Name")
    }
}

internal object TestObjectFactory : EntityFactory<TestEntity, TestTable>() {
    override val table = TestTable()

    override fun entity(): TestEntity {
        return TestEntity {
            firstName = "Default Name"
            email = "Default Email"
        }
    }
}

private object TransformingTestObjectFactory : EntityFactory<TestEntity, TestTable>() {
    override val table = TestTable()

    override fun entity(): TestEntity {
        return TestEntity {
            firstName = "Default Name"
            email = "Default Email"
        }
    }

    override fun transform(name: String, value: Any?): Any? {
        return if (name == "firstName") {
            value?.toString()?.toUpperCase()
        } else {
            value
        }
    }
}

internal interface TestEntity : OzoneEntity<TestEntity> {
    val id: Long
    var firstName: String
    var email: String
    var createdAt: Int
    var updatedAt: Int

    companion object : OzoneEntity.Of<TestEntity>()
}

internal class TestTable : OzoneTable<TestEntity>("test_table") {
    val id by bigIncrements()
    val firstName by string("first_name").bindTo { it.firstName }
    val email by string("email").bindTo { it.email }
    val createdAt by int("created_at").bindTo { it.createdAt }
    val updatedAt by int("updated_at").bindTo { it.updatedAt }

    companion object {
        val createSql = """
        create table test_table(
            id integer primary key autoincrement,
            first_name varchar(128) not null,
            email varchar(128) not null,
            created_at integer,
            updated_at integer
        );
    """.trimIndent()
    }
}
