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
import dev.alpas.lodestar.migration.table.column.AutoIncrementColumn

internal class SqliteAdapter(connection: ConnectionInterface) : DbAdapter(connection) {
    override fun createTable(tableName: String, tableBuilder: TableBuilder) {
        var sql = "CREATE TABLE $tableName (\n"
        // if (tableBuilder.id) {
        //     sql += "  id INTEGER PRIMARY KEY AUTOINCREMENT"
        //     if (tableBuilder.columnList.size > 0) sql += ','
        //     sql += "\n"
        // }
        sql += tableBuilder.columnList.joinToString(",\n") {
            "  " + buildColumnDeclarationForCreateTableSql(it)
        }
        sql += "\n);"
        execute(sql)
    }

    override fun createIndex(tableName: String, columnNameArray: Array<String>, unique: Boolean, method: IndexMethod?) {
        var sql = "CREATE "
        if (unique) sql += "UNIQUE "
        sql += "INDEX ${tableName}_${columnNameArray.joinToString("_")}_idx"
        sql += " ON $tableName (${columnNameArray.joinToString(",")});"
        execute(sql)
    }

    override fun dropIndex(tableName: String, indexName: String) {
        val sql = "DROP INDEX $indexName;"
        execute(sql)
    }

    override fun addColumn(tableName: String, column: AbstractColumn, option: AddingColumnOption) {
        var sql = "ALTER TABLE $tableName ADD COLUMN "
        sql += buildColumnDeclarationForCreateTableSql(column)
        sql += ";"
        execute(sql)
    }

    override fun renameTable(oldTableName: String, newTableName: String) {
        val sql = "ALTER TABLE $oldTableName RENAME TO $newTableName;"
        execute(sql)
    }

    override fun renameIndex(tableName: String, oldIndexName: String, newIndexName: String) {
        // SQLite must drop index and create new index
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addForeignKey(
        tableName: String,
        columnName: String,
        referencedTableName: String,
        referencedColumnName: String
    ) {
        // SQLite doesn't support add Foreign Key function.
        // Foreign key must be added on table creation.
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dropForeignKey(tableName: String, columnName: String, keyName: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    internal companion object : DbAdapter.CompanionInterface() {
        private fun buildColumnDeclarationForCreateTableSql(column: AbstractColumn): String {
            var sql = column.name + " " + sqlType(column)
            if (!column.isNullable) sql += " NOT NULL"
            if (column.hasDefault) sql += " DEFAULT " + column.sqlDefault
            if (column.isPrimary) sql += " PRIMARY KEY"
            if (column.hasReference) sql += " REFERENCES ${column.referenceTable} (${column.referenceColumn})"
            return sql
        }

        override fun sqlType(column: AbstractColumn): String {
            return when (column) {
                is AutoIncrementColumn -> " INTEGER AUTOINCREMENT"
                else -> super.sqlType(column)
            }
        }

        override fun sqlIndexMethod(method: IndexMethod?): String? {
            return null
        }
    }
}
