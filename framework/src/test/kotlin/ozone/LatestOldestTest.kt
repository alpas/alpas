package dev.alpas.tests.ozone

import dev.alpas.ozone.faker
import dev.alpas.ozone.from
import dev.alpas.ozone.latest
import dev.alpas.ozone.oldest
import dev.alpas.tests.BaseTest
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.entity.asSequence
import me.liuwj.ktorm.entity.map
import me.liuwj.ktorm.support.sqlite.SQLiteDialect
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.sql.Connection
import java.sql.DriverManager

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LatestOldestTest : BaseTest() {
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
    fun `latest returns entities sorted by created_at in desc order`() {
        execSqlScript(TestTable.createSql)

        from(TestObjectFactory, 10) {
            it.createdAt to faker.random().nextInt(1, 200)
            it.updatedAt to faker.random().nextInt(1, 200)
        }

        val latest = TestTable().latest().map { it.createdAt }
        var lastCreatedAt = 1000
        latest.forEach {
            assertTrue(it <= lastCreatedAt)
            lastCreatedAt = it
        }
    }

    @Test
    fun `can return the latest entities on an entity sequence`() {
        execSqlScript(TestTable.createSql)

        from(TestObjectFactory, 10) {
            it.createdAt to faker.random().nextInt(1, 200)
            it.updatedAt to faker.random().nextInt(1, 200)
        }

        val latest = TestTable().asSequence().latest().map { it.createdAt }
        var lastCreatedAt = 1000
        latest.forEach {
            assertTrue(it <= lastCreatedAt)
            lastCreatedAt = it
        }
    }

    @Test
    fun `can pick the column for returning the latest entities`() {
        execSqlScript(TestTable.createSql)

        from(TestObjectFactory, 10) {
            it.createdAt to faker.random().nextInt(1, 200)
            it.updatedAt to faker.random().nextInt(1, 200)
        }

        val latest = TestTable().latest("updated_at").map { it.updatedAt }
        var lastUpdatedAt = 1000
        latest.forEach {
            assertTrue(it <= lastUpdatedAt)
            lastUpdatedAt = it
        }
    }

    @Test
    fun `can pass a closure for returning the latest entities`() {
        execSqlScript(TestTable.createSql)

        from(TestObjectFactory, 10) {
            it.createdAt to faker.random().nextInt(1, 200)
            it.updatedAt to faker.random().nextInt(1, 200)
        }

        val latest = TestTable().latest { it.updatedAt }.map { it.updatedAt }
        var lastUpdatedAt = 1000
        latest.forEach {
            assertTrue(it <= lastUpdatedAt)
            lastUpdatedAt = it
        }
    }

    @Test
    fun `can return the latest entities on an entity sequence using a closure`() {
        execSqlScript(TestTable.createSql)

        from(TestObjectFactory, 10) {
            it.createdAt to faker.random().nextInt(1, 200)
            it.updatedAt to faker.random().nextInt(1, 200)
        }

        val latest = TestTable().asSequence().latest { it.updatedAt }.map { it.updatedAt }
        var lastCreatedAt = 1000
        latest.forEach {
            assertTrue(it <= lastCreatedAt)
            lastCreatedAt = it
        }
    }

    @Test
    fun `oldest returns entities sorted by created_at in desc order`() {
        execSqlScript(TestTable.createSql)

        from(TestObjectFactory, 10) {
            it.createdAt to faker.random().nextInt(1, 200)
            it.updatedAt to faker.random().nextInt(1, 200)
        }

        val latest = TestTable().oldest().map { it.createdAt }
        var lastCreatedAt = 0
        latest.forEach {
            assertTrue(it >= lastCreatedAt)
            lastCreatedAt = it
        }
    }

    @Test
    fun `can return the oldest entities on an entity sequence`() {
        execSqlScript(TestTable.createSql)

        from(TestObjectFactory, 10) {
            it.createdAt to faker.random().nextInt(1, 200)
            it.updatedAt to faker.random().nextInt(1, 200)
        }

        val latest = TestTable().asSequence().oldest().map { it.createdAt }
        var lastCreatedAt = 0
        latest.forEach {
            assertTrue(it >= lastCreatedAt)
            lastCreatedAt = it
        }
    }

    @Test
    fun `can return the oldest entities on an entity sequence using a closure`() {
        execSqlScript(TestTable.createSql)

        from(TestObjectFactory, 10) {
            it.createdAt to faker.random().nextInt(1, 200)
            it.updatedAt to faker.random().nextInt(1, 200)
        }

        val latest = TestTable().asSequence().oldest { it.updatedAt }.map { it.updatedAt }
        var lastUpdatedAt = 0
        latest.forEach {
            assertTrue(it >= lastUpdatedAt)
            lastUpdatedAt = it
        }
    }

    @Test
    fun `can pick the column for returning the oldest entities`() {
        execSqlScript(TestTable.createSql)

        from(TestObjectFactory, 10) {
            it.createdAt to faker.random().nextInt(1, 200)
            it.updatedAt to faker.random().nextInt(1, 200)
        }

        val latest = TestTable().oldest("updated_at").map { it.updatedAt }
        var lastUpdatedAt = 0
        latest.forEach {
            assertTrue(it >= lastUpdatedAt)
            lastUpdatedAt = it
        }
    }

    @Test
    fun `can pass a closure for returning the oldest entities`() {
        execSqlScript(TestTable.createSql)

        from(TestObjectFactory, 10) {
            it.createdAt to faker.random().nextInt(1, 200)
            it.updatedAt to faker.random().nextInt(1, 200)
        }

        val latest = TestTable().oldest { it.updatedAt }.map { it.updatedAt }
        var lastUpdatedAt = 0
        latest.forEach {
            assertTrue(it >= lastUpdatedAt)
            lastUpdatedAt = it
        }
    }
}
