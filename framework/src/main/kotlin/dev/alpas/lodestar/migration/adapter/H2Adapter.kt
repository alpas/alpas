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

import dev.alpas.lodestar.migration.table.IndexMethod
import dev.alpas.lodestar.migration.table.TableBuilder
import dev.alpas.lodestar.migration.ConnectionInterface
import dev.alpas.lodestar.migration.table.column.AbstractColumn
import dev.alpas.lodestar.migration.table.column.AddingColumnOption

internal class H2Adapter(connection: ConnectionInterface) : DbAdapter(connection) {
    override fun createTable(tableName: String, tableBuilder: TableBuilder) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createIndex(
        tableName: String,
        columnNameArray: Array<String>,
        unique: Boolean,
        method: IndexMethod?
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dropIndex(tableName: String, indexName: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addColumn(
        tableName: String,
        column: AbstractColumn,
        option: AddingColumnOption
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun renameTable(oldTableName: String, newTableName: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun renameIndex(
        tableName: String,
        oldIndexName: String,
        newIndexName: String
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addForeignKey(
        tableName: String,
        columnName: String,
        referencedTableName: String,
        referencedColumnName: String
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dropForeignKey(
        tableName: String,
        columnName: String,
        keyName: String
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
