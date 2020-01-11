package dev.alpas.ozone.migration

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table

@Suppress("unused")
abstract class Migration {
    internal lateinit var filename: String

    fun <T : Table> createTable(table: T, vararg tables: T, inBatch: Boolean = false) {
        SchemaUtils.create(table, *tables, inBatch = inBatch)
    }

    fun dropTable(table: Table, vararg tables: Table, inBatch: Boolean = false) {
        SchemaUtils.drop(table, *tables, inBatch = inBatch)
    }

    fun modifyTable(table: Table, vararg tables: Table, inBatch: Boolean = false) {
        SchemaUtils.createMissingTablesAndColumns(table, *tables, inBatch = inBatch)
    }

    open fun up() {}
    open fun down() {}
}
