package dev.alpas.ozone.migration

import dev.alpas.isOneOf
import dev.alpas.ozone.*
import me.liuwj.ktorm.schema.Column
import java.sql.Types

open class TableBuilder(val tableName: String) {
    internal val columnsToAdd = mutableListOf<ColumnInfo>()
    internal var keys = mutableSetOf<ColumnKey>()
    internal var constraints = mutableSetOf<ColumnReferenceConstraint>()

    open fun <E : Ozone<E>> addColumn(col: Column<*>, table: OzoneTable<E>) {
        val meta = table.metadataMap[col.name]
        columnsToAdd.add(ColumnInfo(col, meta))
    }

    fun primaryKey(columnName: String) {
        keys.add(ColumnKey("PRIMARY KEY", setOf(columnName)))
    }

    fun uniqueKey(columnName: String, name: String? = null) {
        keys.add(
            ColumnKey(
                "UNIQUE KEY",
                setOf(columnName),
                name ?: "`${tableName}_${columnName}_unique`"
            )
        )
    }

    fun addIndex(columnName: String, name: String? = null) {
        keys.add(ColumnKey("KEY", setOf(columnName), name ?: "`${tableName}_${columnName}_index`"))
    }

    fun addIndex(columnNames: Iterable<String>, name: String? = null) {
        keys.add(
            ColumnKey(
                "KEY",
                columnNames.toSet(),
                name ?: "`${tableName}_${columnNames.joinToString("_")}_index`"
            )
        )
    }

    internal fun normalize() {
        columnsToAdd.forEach {
            if (it.meta == null) {
                it.meta = ColumnMetadata()
            }
            // varchar must have a size definied
            if (it.col.sqlType.isVarChar() && it.meta?.size == null) {
                it.meta = it.meta?.copy(size = 255)
            }
            if (it.meta?.unique == true) {
                uniqueKey(it.col.name)
            }

        }
    }

    fun addIndex(column: Column<String>, name: String? = null) {
        addIndex(column.name, name)
    }

    fun addReference(
        foreignKey: String,
        tableToRefer: String,
        columnToRefer: String? = null
    ): ColumnReferenceConstraint {
        return ColumnReferenceConstraint(foreignKey, tableToRefer, columnToRefer ?: "id").also {
            constraints.add(it)
        }
    }

    fun <E : Ozone<E>> addReference(
        foreignColumn: Column<*>,
        tableToRefer: OzoneTable<E>,
        columnToRefer: Column<*>? = null
    ): ColumnReferenceConstraint {
        checkColumnsTypes(foreignColumn, tableToRefer, columnToRefer)
        return addReference(foreignColumn.name, tableToRefer.tableName, columnToRefer?.name)
    }

    private fun <E : Ozone<E>> checkColumnsTypes(
        foreignColumn: Column<*>,
        tableToRefer: OzoneTable<E>,
        columnToRefer: Column<*>?
    ) {
        // If the referencing column is unsigned but the foreign key column isn't then adding constraint
        // fails with not so friendly error message. We'll check if that's the case here and throw an
        // exception with a proper message to help out the user and also telling how to fix it..
        if (foreignColumn.sqlType.typeCode.isOneOf(Types.BIGINT, Types.INTEGER, Types.SMALLINT, Types.TINYINT)) {
            val foreignColName = foreignColumn.name
            val referredColName = columnToRefer?.name ?: "id"

            val foreignKeyColumnIsUnsigned =
                columnsToAdd.first { it.col.name == foreignColName }.meta?.unsigned ?: false
            val referredColumnIsUnsigned = tableToRefer.metadataMap[referredColName]?.unsigned == true

            check(referredColumnIsUnsigned == foreignKeyColumnIsUnsigned) {
                val foreignKeyInfo = "${tableName}.$foreignColName"
                val referredKeyInfo = "${tableToRefer.tableName}.$referredColName"
                "Type mismatch between '$foreignKeyInfo' and '$referredKeyInfo'. Make sure they are of same sql type and that both are either signed or unsigned."
            }
        }
    }
}

open class TableModifier<E : Ozone<E>>(val table: OzoneTable<E>) : TableBuilder(table.tableName) {
    internal val columnsToDrop = mutableListOf<String>()
    fun addColumn(column: Column<*>): ColumnInfo {
        val meta = table.metadataMap[column.name]
        return ColumnInfo(column, meta).also {
            columnsToAdd.add(it)
        }
    }

    fun dropColumn(name: String, vararg names: String) {
        columnsToDrop.add(name)
        columnsToDrop.addAll(names)
    }
}
