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

internal open class IntegerColumn(name: String) : AbstractColumn(name) {
    override var sqlDefault: String? = null

    var default: Long?
        get() = sqlDefault?.toLongOrNull()
        set(value) {
            sqlDefault = value?.toString()
        }

    var unsigned = false
}

internal class BigIntegerColumn(name: String) : IntegerColumn(name)

internal class AutoIncrementColumn(name: String) : AbstractColumn(name) {
    override var sqlDefault: String? = null
    var isBigInt = false

    init {
        primary()
    }

    fun primary() {
        isPrimary = true
    }
}
