package dev.alpas.ozone.migration

import dev.alpas.ozone.ColumnInfo
import dev.alpas.ozone.ColumnMetadata
import me.liuwj.ktorm.database.TransactionIsolation
import me.liuwj.ktorm.database.useTransaction

internal class SqliteAdapter(isDryRun: Boolean, quiet: Boolean) : DbAdapter(isDryRun, quiet) {
    override fun createTable(tableBuilder: TableBuilder, ifNotExists: Boolean) {
        val notExists = if (ifNotExists) " IF NOT EXISTS " else " "
        val sb = StringBuilder("CREATE TABLE$notExists${tableBuilder.tableName}")
        sb.appendln(" (")
        val colDef = tableBuilder.columns.joinToString(",\n") {
            columnDefinition(it)
        }
        sb.appendln(colDef)
        sb.append(");")
        execute(sb.toString())
    }

    override fun execute(sql: String): Boolean {
        return if (isDryRun) {
            super.execute(sql)
        } else {
            useTransaction(TransactionIsolation.SERIALIZABLE) {
                it.connection.prepareStatement(sql).use { statement ->
                    statement.execute()
                }
            }
        }
    }

    private fun columnDefinition(colInfo: ColumnInfo): String {
        return "`${colInfo.col.name}` ${toColTypeName(colInfo)}${colInfo.meta.def()}"
    }

    private fun toColTypeName(colInfo: ColumnInfo): String {
        // sqlite only allows integer for autoincrement field
        if (colInfo.col.sqlType.typeCode == java.sql.Types.INTEGER || colInfo.meta?.autoIncrement == true) {
            return "integer"
        }
        return colInfo.col.sqlType.typeName.toLowerCase()
    }

    private fun ColumnMetadata?.def(): String {
        if (this == null) {
            return ""
        }
        val sb = StringBuilder()
        size?.let {
            sb.append("($it)")
        }
        if (autoIncrement) {
            sb.append(" PRIMARY KEY AUTOINCREMENT")
        }
        if (nullable) {
            sb.append(" NULL DEFAULT NULL")
        } else {
            sb.append(" NOT NULL")
            defaultValue?.let { dval ->
                sb.append(" DEFAULT $dval")
            }
        }

        return sb.toString()
    }
}
