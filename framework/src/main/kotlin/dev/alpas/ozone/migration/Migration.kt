package dev.alpas.ozone.migration

import dev.alpas.ozone.MigratingTable
import me.liuwj.ktorm.entity.Entity

// todo: support adding composite keys
// todo: support collation
// todo: support adapters other than MySQL
// todo: support altering table

abstract class Migration {
    // internal var isDryRun = false
    internal lateinit var filename: String
    internal lateinit var adapter: DbAdapter

    fun <E : Entity<E>> createTable(
        table: MigratingTable<E>,
        ifNotExists: Boolean = false,
        block: (TableBuilder.() -> Unit) = {}
    ) {
        val cols = table.columns
        adapter.createTable(table.tableName, ifNotExists) {
            cols.forEach {
                addColumn(it, table)
            }

            table.primaryKey?.name?.let {
                primaryKey(it)
            }
            block()
        }
    }

    fun <E : Entity<E>> createTable(
        table: MigratingTable<E>,
        vararg tables: MigratingTable<E>,
        ifNotExists: Boolean = false
    ) {
        createTable(table, ifNotExists)
        tables.forEach { createTable(it, ifNotExists) }
    }

    fun <E : Entity<E>> dropTable(table: MigratingTable<E>, vararg tables: MigratingTable<E>) {
        adapter.dropTable(table.tableName)
        tables.forEach { adapter.dropTable(it.tableName) }
    }

    open fun up() {}
    open fun down() {}
}
