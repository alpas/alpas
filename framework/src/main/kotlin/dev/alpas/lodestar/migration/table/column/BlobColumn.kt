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

internal class BlobColumn(name: String) : AbstractColumn(name) {
    var default: ByteArray? = null
        set(value) {
            field = value
            sqlDefault = value?.let { "E'\\\\x" + it.toHexString() + "'" }
        }

    override var sqlDefault: String? = null
}

private fun ByteArray.toHexString(): String {
    // todo: change to actual hex string
    return ""
}
