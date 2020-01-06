package dev.alpas.ozone.migration

import dev.alpas.printAsSuccess
import dev.alpas.printAsWarning
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.name
import org.jetbrains.exposed.sql.transactions.transaction

internal class MySqlAdapter(isDryRun: Boolean, quiet: Boolean, db: Database) : DbAdapter(isDryRun, quiet, db) {
    override fun createDatabase(name: String): Boolean {
        return execute("CREATE DATABASE `$name`")
    }

    override fun dropAllTables() {
        try {
            execute("SET FOREIGN_KEY_CHECKS = 0")

            if (shouldTalk) {
                "Dropping all tables of ${db.name}".printAsWarning()
            }
            val tableNames = mutableListOf<String>()
            val selectSql = "SELECT table_name FROM information_schema.tables WHERE table_schema = '$db'"
            transaction {
                val query = connection.prepareStatement(selectSql, true).executeQuery()
                while (query.next()) {
                    val tableName = query.getString("table_name")
                    tableNames.add("`$tableName`")
                }
            }

            val sql = tableNames.joinToString(",")
            if (sql.isNotEmpty()) {
                execute("DROP TABLE IF EXISTS $sql")
                if (shouldTalk) {
                    "Done!".printAsSuccess()
                }
            }
        } finally {
            execute("SET FOREIGN_KEY_CHECKS = 1")
        }
    }
}
