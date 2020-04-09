package dev.alpas.ozone.migration

import com.github.ajalt.clikt.output.TermUi.echo
import dev.alpas.deleteLastLine
import dev.alpas.ozone.*
import dev.alpas.terminalColors
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.database.useConnection
import me.liuwj.ktorm.jackson.JsonSqlType
import me.liuwj.ktorm.schema.BlobSqlType
import me.liuwj.ktorm.schema.Column
import me.liuwj.ktorm.schema.TextSqlType

internal class MySqlAdapter(isDryRun: Boolean, quiet: Boolean) : DbAdapter(isDryRun, quiet) {
    override fun createTable(tableBuilder: TableBuilder, ifNotExists: Boolean) {
        val notExists = if (ifNotExists) " IF NOT EXISTS " else " "
        val sb = StringBuilder("CREATE TABLE$notExists`${tableBuilder.tableName}`")
        sb.appendln(" (")
        val colDef = tableBuilder.columnsToAdd.joinToString(",\n") {
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
        return "`${colInfo.col.name}` ${toColTypeName(colInfo.col)}${colInfo.meta.def(colInfo)}"
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

    private fun ColumnMetadata?.def(colInfo: ColumnInfo): String {
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
                sb.append(" DEFAULT ")
                val sqlType = colInfo.col.sqlType
                if (sqlType is JsonSqlType || sqlType is BlobSqlType || sqlType is TextSqlType) {
                    sb.append("('$dval')")
                } else {
                    sb.append("$dval")
                }
            }
        }
        if (autoIncrement) {
            sb.append(" AUTO_INCREMENT")
        }
        after?.let {
            sb.append(" after $it")
        }
        return sb.toString()
    }

    override fun dropAllTables() {
        try {
            execute("SET FOREIGN_KEY_CHECKS = 0")
            val db = Database.global.name

            if (shouldTalk) {
                terminalColors.apply {
                    echo("${yellow("Dropping all the tables of")} ${brightYellow(db)}")
                }
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
                    terminalColors.apply {
                        deleteLastLine()
                        echo("${brightGreen("✓ Dropped all the tables of")} ${brightYellow(db)}")
                    }
                }
            }
        } finally {
            execute("SET FOREIGN_KEY_CHECKS = 1")
        }
    }

    override fun <E : OzoneEntity<E>> modifyTable(builder: TableModifier<E>) {
        val sb = StringBuilder("ALTER TABLE `${builder.tableName}`")
        addNewColumns(sb, builder)
        dropColumns(sb, builder)
        sb.append(";")
        execute(sb.toString())
    }

    private fun <E : OzoneEntity<E>> addNewColumns(sb: StringBuilder, builder: TableModifier<E>) {
        if (builder.columnsToAdd.isEmpty()) {
            return
        }
        val colDef = builder.columnsToAdd.joinToString(",\n", prefix = "\n") {
            "ADD COLUMN ${columnDefinition(it)}"
        }
        sb.appendln(colDef)

        val keysDef = builder.keys.joinToString(",\n") {
            columnKeysDefinition(it)
        }
        if (keysDef.isNotEmpty()) {
            sb.append(",")
            sb.appendln(keysDef)
        }

        val constraintsDef = builder.constraints.joinToString(",\n") {
            columnConstraints(it, builder.tableName)
        }
        if (constraintsDef.isNotEmpty()) {
            sb.append(",")
            sb.appendln(constraintsDef)
        }
    }

    private fun <E : OzoneEntity<E>> dropColumns(sb: StringBuilder, builder: TableModifier<E>) {
        if (builder.columnsToDrop.isEmpty()) {
            return
        }

        val dropColumnsDef = builder.columnsToDrop.joinToString(",\n", prefix = "\n") {
            "DROP COLUMN $it"
        }
        sb.appendln(dropColumnsDef)
    }
}
