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

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

internal abstract class AbstractDateTimeColumn(
    name: String
) : AbstractColumn(name) {
    private val formatter = DateTimeFormatter.ofPattern(
        "yyyy-M[M]-d[d][ H[H]:m[m][:s[s]][.SSS][ zzz]]"
    )
    var default: String?
        get() {
            return defaultLocalDateTime?.toString()
        }
        set(value) {
            defaultLocalDateTime = value?.let {
                LocalDateTime.parse(it, formatter)
            }
        }
    var defaultLocalDateTime: LocalDateTime? = null
        set(value) {
            field = value
            sqlDefault =
                value?.let { "'" + defaultLocalDateTime.toString() + "'" }
        }
    var defaultDate: Date?
        get() {
            return defaultLocalDateTime?.let {
                Date.from(
                    ZonedDateTime.of(
                        it,
                        ZoneId.systemDefault()
                    ).toInstant()
                )
            }
        }
        set(value) {
            defaultLocalDateTime = value?.let {
                LocalDateTime.ofInstant(it.toInstant(), ZoneId.systemDefault())
            }
        }

    override var sqlDefault: String? = null
}
