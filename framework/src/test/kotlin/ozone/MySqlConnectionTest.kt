package dev.alpas.tests.ozone

import dev.alpas.Environment
import dev.alpas.ozone.ConnectionConfig
import dev.alpas.ozone.CustomMySqlFormatter
import dev.alpas.ozone.MySqlConnection
import dev.alpas.ozone.MySqlDialect
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.database.SqlDialect
import me.liuwj.ktorm.expression.SqlFormatter
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class MySqlConnectionTest {
    @Test
    fun `sets default db connections from the environment values`(@RelaxedMockK env: Environment) {
        env.fillWithTestValues()
        val connection = MySqlConnection(env)
        assertEquals("jdbc:mysql://testhost:3333/testdb?useSSL=false&serverTimezone=UTC", connection.jdbcUrl)
    }

    @Test
    fun `can pass extra params`(@RelaxedMockK env: Environment) {
        env.fillWithTestValues()
        val connection = MySqlConnection(env, ConnectionConfig(extraParams = mapOf("allowPublicKeyRetrieval" to true)))
        assertEquals("jdbc:mysql://testhost:3333/testdb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true", connection.jdbcUrl)
    }

    @Test
    fun `connection config values take precedence over environment variables`(@RelaxedMockK env: Environment) {
        env.fillWithTestValues()
        val connection = MySqlConnection(env, ConnectionConfig(host = "differenthost", port = 1111, useSSL = true))
        assertEquals("jdbc:mysql://differenthost:1111/testdb?useSSL=true&serverTimezone=UTC", connection.jdbcUrl)
    }

    @Test
    fun `mysql dialect is loaded by default`(@RelaxedMockK env: Environment) {
        env.fillWithTestValues()
        val connection = MySqlConnection(env, ConnectionConfig(host = "differenthost", port = 1111, useSSL = true))
        val dialect = connection.dialect
        assertNotNull(dialect)
        assertTrue(dialect is MySqlDialect)
    }

    @Test
    fun `dialect can be overriden`(@RelaxedMockK env: Environment) {
        env.fillWithTestValues()
        val testDialect = object : SqlDialect {
            override fun createSqlFormatter(database: Database, beautifySql: Boolean, indentSize: Int): SqlFormatter {
                return mockk<CustomMySqlFormatter>(relaxed = true)
            }
        }

        val connection = MySqlConnection(
            env,
            ConnectionConfig(host = "differenthost", port = 1111, useSSL = true, sqlDialect = testDialect)
        )
        val dialect = connection.dialect
        assertEquals(testDialect, dialect)
        assertFalse(dialect is MySqlDialect)
    }

    private fun Environment.fillWithTestValues() {
        val env = this
        every { env("DB_HOST", any<String>()) } returns "testhost"
        every { env("DB_PORT", any<Int>()) } returns 3333
        every { env("DB_DATABASE", any<String>()) } returns "testdb"
        every { env("DB_USERNAME", any<String>()) } returns "testusername"
        every { env("DB_USE_SSL", any<Boolean>()) } returns false
    }
}
