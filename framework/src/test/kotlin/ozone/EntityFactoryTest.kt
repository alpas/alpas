package dev.alpas.tests.ozone

import dev.alpas.ozone.*
import dev.alpas.tests.BaseTest
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.entity.Entity
import me.liuwj.ktorm.entity.findAll
import me.liuwj.ktorm.entity.findById
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
    fun `can override entity's properties`() {
        TestObjectFactory.build().apply {
            assertEquals("Default Name", name)
            assertEquals("Default Email", email)
        }

        TestObjectFactory.build(mapOf("name" to "New Name")).apply {
            assertEquals("New Name", name)
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

        assertNotEquals("Default Name", dbEntity?.name)
        assertEquals("DEFAULT NAME", dbEntity?.name)
        assertEquals("Default Email", dbEntity?.email)
    }

    @Test
    fun `can override persistent entity's properties`() {
        execSqlScript(TestTable.createSql)

        val entity = from(TestObjectFactory, "name" to "Test Name")
        val all = TestTable().findAll()
        assertEquals(1, all.size)
        val dbEntity = TestTable().findById(entity.id)
        assertNotNull(dbEntity)
        assertEquals("Test Name", dbEntity?.name)
    }

    @Test
    fun `extra properties will be ignored`() {
        execSqlScript(TestTable.createSql)

        val entity = from(TestObjectFactory, "name" to "Test Name", "address" to "Random Street")
        val all = TestTable().findAll()
        assertEquals(1, all.size)
        val dbEntity = TestTable().findById(entity.id)
        assertNotNull(dbEntity)
        assertEquals("Test Name", dbEntity?.name)
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

        from(TestObjectFactory, 5, "name" to "Same Name")
        val all = TestTable().findAll()
        assertEquals(5, all.size)

        assertEquals(all.random().name, "Same Name")
    }

    @Test
    fun `creating many entities returns fresh copies`() {
        execSqlScript(TestTable.createSql)

        val entities = from(TestObjectFactory, 10, "name" to "Same Name")
        assertEquals(10, entities.size)

        assertNotNull(entities.random().id)
        assertEquals(entities.random().name, "Same Name")
    }
}

object TestObjectFactory : EntityFactory<TestEntity> {
    override val table = TestTable()

    override fun entity(): TestEntity {
        return TestEntity {
            name = "Default Name"
            email = "Default Email"
        }
    }
}

object TransformingTestObjectFactory : EntityFactory<TestEntity> {
    override val table = TestTable()

    override fun entity(): TestEntity {
        return TestEntity {
            name = "Default Name"
            email = "Default Email"
        }
    }

    override fun transform(name: String, value: Any?): Any? {
        return if(name == "name") {
            value?.toString()?.toUpperCase()
        } else {
            value
        }
    }
}

interface TestEntity : Entity<TestEntity> {
    val id: Long
    var name: String
    var email: String

    companion object : Entity.Factory<TestEntity>()
}

class TestTable : MigratingTable<TestEntity>("test_table") {
    val id by bigIncrements("id").bindTo { it.id }
    val name by string("name").bindTo { it.name }
    val email by string("email").bindTo { it.email }

    companion object {
        val createSql = """
        create table test_table(
            id integer primary key autoincrement,
            name varchar(128) not null,
            email varchar(128) not null
        );
    """.trimIndent()
    }
}



