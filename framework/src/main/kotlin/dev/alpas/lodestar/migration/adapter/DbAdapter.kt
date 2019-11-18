/*
 * Copyright (C) 2019  Kenji Otsuka
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.alpas.lodestar.migration.adapter

import dev.alpas.lodestar.migration.ConnectionInterface
import dev.alpas.lodestar.migration.table.IndexMethod
import dev.alpas.lodestar.migration.table.TableBuilder
import dev.alpas.lodestar.migration.table.column.AbstractColumn
import dev.alpas.lodestar.migration.table.column.AddingColumnOption
import dev.alpas.lodestar.migration.table.column.BlobColumn
import dev.alpas.lodestar.migration.table.column.BooleanColumn
import dev.alpas.lodestar.migration.table.column.DateColumn
import dev.alpas.lodestar.migration.table.column.DateTimeColumn
import dev.alpas.lodestar.migration.table.column.DecimalColumn
import dev.alpas.lodestar.migration.table.column.IntegerColumn
import dev.alpas.lodestar.migration.table.column.JsonColumn
import dev.alpas.lodestar.migration.table.column.TextColumn
import dev.alpas.lodestar.migration.table.column.TimeColumn
import dev.alpas.lodestar.migration.table.column.TimestampColumn
import dev.alpas.lodestar.migration.table.column.VarcharColumn
import dev.alpas.printAsError
import dev.alpas.printAsInfo

internal abstract class DbAdapter(private val connection: ConnectionInterface) {

    fun createTable(tableName: String, block: TableBuilder.() -> Any) {
        createTable(tableName, TableBuilder().apply { block() })
    }

    protected fun execute(sql: String) {
        if (connection.isDryRun) {
            sql.printAsInfo()
        } else {
            connection.execute(sql)
        }
    }

    abstract fun createTable(tableName: String, tableBuilder: TableBuilder)

    fun dropTable(tableName: String) {
        execute("DROP TABLE $tableName")
    }

    abstract fun createIndex(
        tableName: String, columnNameArray: Array<String>, unique: Boolean = false,
        method: IndexMethod? = null
    )

    abstract fun dropIndex(tableName: String, indexName: String)

    abstract fun addColumn(tableName: String, column: AbstractColumn, option: AddingColumnOption)

    fun removeColumn(tableName: String, columnName: String) {
        execute("ALTER TABLE $tableName DROP COLUMN $columnName;")
    }

    abstract fun renameTable(oldTableName: String, newTableName: String)

    fun renameColumn(tableName: String, oldColumnName: String, newColumnName: String) {
        execute("ALTER TABLE $tableName RENAME COLUMN $oldColumnName TO $newColumnName")
    }

    abstract fun renameIndex(tableName: String, oldIndexName: String, newIndexName: String)

    abstract fun addForeignKey(
        tableName: String,
        columnName: String,
        referencedTableName: String,
        referencedColumnName: String
    )

    fun dropForeignKey(tableName: String, columnName: String) {
        dropForeignKey(
            tableName, columnName,
            buildForeignKeyName(tableName, columnName)
        )
    }

    abstract fun dropForeignKey(tableName: String, columnName: String, keyName: String)

    protected open fun buildForeignKeyName(tableName: String, columnName: String) = "${tableName}_${columnName}_fkey"
    open fun createDatabase(name: String) {
        "Creating database is not supported.".printAsError()
    }

    internal abstract class CompanionInterface {
        protected open fun sqlType(column: AbstractColumn): String {
            return when (column) {
                is IntegerColumn -> "INTEGER"
                is VarcharColumn -> "VARCHAR"
                is DecimalColumn -> "DECIMAL"
                is BooleanColumn -> "BOOL"
                is TextColumn -> "TEXT"
                is JsonColumn -> "TEXT"
                is BlobColumn -> "BLOB"
                is DateColumn -> "DATE"
                is TimeColumn -> "TIME"
                is DateTimeColumn -> "DATETIME"
                is TimestampColumn -> "TIMESTAMP"
                else -> throw Exception()
            }
        }

        protected abstract fun sqlIndexMethod(method: IndexMethod?): String?
    }
}
