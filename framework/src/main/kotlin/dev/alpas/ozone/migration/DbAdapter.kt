package dev.alpas.ozone.migration

import dev.alpas.ozone.isMySql
import dev.alpas.ozone.isSqlite
import dev.alpas.printAsError
import dev.alpas.printAsInfo
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.database.useConnection

abstract class DbAdapter(val isDryRun: Boolean = false) {
    fun createTable(tableName: String, ifNotExists: Boolean = false, block: TableBuilder.() -> Any) {
        createTable(TableBuilder(tableName).apply {
            block()
            normalize()
        }, ifNotExists)
    }

    protected open fun execute(sql: String): Boolean {
        return if (isDryRun) {
            sql.printAsInfo()
            true
        } else {
            useConnection {
                it.prepareStatement(sql).use { statement ->
                    statement.execute()
                }
            }
        }
    }

    fun dropTable(tableName: String) {
        execute("DROP TABLE `$tableName`")
    }

    open fun dropAllTables() {
    }

    open fun createDatabase(name: String): Boolean {
        "Creating database is not supported for connection: `${Database.global.productName}`.".printAsError()
        return false
    }

    abstract fun createTable(tableBuilder: TableBuilder, ifNotExists: Boolean = false)

    companion object {
        fun make(isDryRun: Boolean): DbAdapter {
            val db = Database.global
            return when {
                db.isMySql() -> MySqlAdapter(isDryRun)
                db.isSqlite() -> SqliteAdapter(isDryRun)
                else -> throw Exception("Database adapter not supported: '${db.productName}'.")
            }
        }
    }
}
