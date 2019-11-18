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

package dev.alpas.lodestar.migration.table.column

class ColumnBuilder internal constructor(internal val column: AbstractColumn) {
    internal fun refer(tableName: String, columnName: String? = null, onDelete: String? = null): ColumnBuilder {
        column.let {
            it.referenceTable = tableName
            it.referenceColumn = columnName
            it.onDelete = onDelete
        }
        return this
    }

    fun comment(text: String): ColumnBuilder {
        column.comment = text
        return this
    }

    internal fun build(): AbstractColumn {
        return column
    }

    fun nullable(): ColumnBuilder {
        column.isNullable = true
        return this
    }

    fun default(default: Any?): ColumnBuilder {
        column.sqlDefault = default?.toString()
        return this
    }
}
