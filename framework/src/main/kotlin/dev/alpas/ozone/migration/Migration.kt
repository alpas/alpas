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
        block: (TableBuilder.() -> Unit)? = null
    ) {
        val cols = table.columns
        adapter.createTable(table.tableName, ifNotExists) {
            cols.forEach {
                addColumn(it, table)
            }

            table.primaryKey?.name?.let {
                primaryKey(it)
            }
            if (block != null) {
                block()
            }
        }
    }

    fun <E : Entity<E>> dropTable(table: MigratingTable<E>) {
        adapter.dropTable(table.tableName)
    }

    open fun up() {}
    open fun down() {}

    protected fun execute(query: String) {
        adapter.execute(query)
    }
}
