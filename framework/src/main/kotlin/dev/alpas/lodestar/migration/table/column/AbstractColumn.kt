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

import dev.alpas.lodestar.migration.Index

internal typealias Type = Int

internal abstract class AbstractColumn(
    val name: String
) {
    val indexList: MutableList<Index> = mutableListOf()

    fun index() {
        indexList.add(Index())
    }

    var isNullable = false
        internal set

    abstract var sqlDefault: String?

    val hasDefault: Boolean
        get() = sqlDefault != null

    open var isPrimary: Boolean = false
        internal set

    var referenceTable: String? = null
    var referenceColumn: String? = null
    var onDelete: String? = null

    val hasReference: Boolean
        get() {
            return !(
                referenceTable.isNullOrBlank() ||
                    referenceColumn.isNullOrBlank()
                )
        }

    var comment: String? = null

    val hasComment: Boolean
        get() {
            return comment != null
        }
}
