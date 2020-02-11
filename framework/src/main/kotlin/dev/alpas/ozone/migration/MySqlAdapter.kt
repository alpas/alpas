package dev.alpas.ozone.migration

import dev.alpas.ozone.ColumnInfo
import dev.alpas.ozone.ColumnKey
import dev.alpas.ozone.ColumnMetadata
import dev.alpas.ozone.ColumnReferenceConstraint
import dev.alpas.printAsSuccess
import dev.alpas.printAsWarning
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.database.useConnection
import me.liuwj.ktorm.schema.Column

internal class MySqlAdapter(isDryRun: Boolean, quiet: Boolean) : DbAdapter(isDryRun, quiet) {
    override fun createTable(tableBuilder: TableBuilder, ifNotExists: Boolean) {
        val notExists = if (ifNotExists) " IF NOT EXISTS " else " "
        val sb = StringBuilder("CREATE TABLE$notExists${tableBuilder.tableName}")
        sb.appendln(" (")
        val colDef = tableBuilder.columns.joinToString(",\n") {
            columnDefinition(it)
        }
        sb.appendln(colDef)

        val keysDef = tableBuilder.keys.joinToString(",\n") {
            columnKeysDefinition(it)
        }
        if (keysDef.isNotEmpty()) {
            sb.append(",")
        }
        sb.appendln(keysDef)

        val constraintsDef = tableBuilder.constraints.joinToString(",\n") {
            columnConstraints(it, tableBuilder.tableName)
        }
        if (constraintsDef.isNotEmpty()) {
            sb.append(",")
        }
        sb.appendln(constraintsDef)

        sb.append(");")
        execute(sb.toString())
    }

    override fun createDatabase(name: String): Boolean {
        return execute("CREATE DATABASE `$name`")
    }

    private fun columnDefinition(colInfo: ColumnInfo): String {
        return "`${colInfo.col.name}` ${toColTypeName(colInfo.col)}${colInfo.meta.def()}"
    }

    private fun columnKeysDefinition(key: ColumnKey): String {
        val columns = key.colNames.joinToString(",", prefix = "`", postfix = "`")
        return "${key.type} ${key.name} ($columns)"
    }

    private fun columnConstraints(constraint: ColumnReferenceConstraint, tableName: String): String {
        val sql =
            "CONSTRAINT `${tableName}_${constraint.foreignKey}_foreign` FOREIGN KEY (`${constraint.foreignKey}`) REFERENCES `${constraint.tableToRefer}` (`${constraint.columnToRefer}`)"
        return constraint.onDelete?.let {
            "$sql ON DELETE $it"
        } ?: sql
    }

    private fun toColTypeName(col: Column<*>): String {
        return col.sqlType.typeName.toLowerCase()
    }

    private fun ColumnMetadata?.def(): String {
        if (this == null) {
            return ""
        }
        val sb = StringBuilder()
        size?.let {
            sb.append("($it)")
        }
        if (unsigned) sb.append(" unsigned")
        if (nullable) {
            sb.append(" NULL DEFAULT NULL")
        } else {
            sb.append(" NOT NULL")
        }
        if (useCurrentTimestamp) {
            sb.append(" DEFAULT CURRENT_TIMESTAMP")
        } else {
            defaultValue?.let { dval ->
                sb.append(" DEFAULT $dval")
            }
        }
        if (autoIncrement) {
            sb.append(" AUTO_INCREMENT")
        }
        return sb.toString()
    }

    override fun dropAllTables() {
        try {
            execute("SET FOREIGN_KEY_CHECKS = 0")
            val db = Database.global.name

            if (shouldTalk) {
                "Dropping all tables of $db".printAsWarning()
            }
            val tableNames = mutableListOf<String>()
            val selectSql = "SELECT table_name FROM information_schema.tables WHERE table_schema = '$db'"
            useConnection {
                it.prepareStatement(selectSql).use { stm ->
                    val query = stm.executeQuery()
                    while (query.next()) {
                        val tableName = query.getString("table_name")
                        tableNames.add("`$tableName`")
                    }
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
