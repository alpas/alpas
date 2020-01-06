package dev.alpas.ozone.migration

import dev.alpas.ozone.isMySql
import dev.alpas.ozone.isSqlite
import dev.alpas.printAsError
import dev.alpas.printAsInfo
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.jdbc.JdbcPreparedStatementImpl
import org.jetbrains.exposed.sql.transactions.transaction

abstract class DbAdapter(val isDryRun: Boolean = false, quiet: Boolean, val db: Database) {
    protected val shouldTalk = !(quiet || isDryRun)

    protected open fun execute(sql: String): Boolean {
        return if (isDryRun) {
            sql.printAsInfo()
            true
        } else {
            transaction {
                this.execute(sql)
            }
        }
    }

    fun dropTable(tableName: String) {
        execute("DROP TABLE `$tableName`")
    }

    open fun dropAllTables() {
    }

    open fun createDatabase(name: String): Boolean {
        "Creating database is not supported.".printAsError()
        return false
    }

    companion object {
        fun make(isDryRun: Boolean, quiet: Boolean, db: Database): DbAdapter {
            return when {
                db.isMySql() -> MySqlAdapter(isDryRun, quiet, db)
                db.isSqlite() -> SqliteAdapter(isDryRun, quiet, db)
                else -> throw Exception("Database adapter not supported: '${db.vendor}'.")
            }
        }
    }

    protected fun Transaction.execute(sql: String): Boolean {
        val preparedStatement = connection.prepareStatement(sql, false) as JdbcPreparedStatementImpl
        return preparedStatement.statement.execute()
    }
}

