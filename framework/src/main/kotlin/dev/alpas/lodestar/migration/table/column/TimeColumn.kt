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

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

internal class TimeColumn(name: String) : AbstractColumn(name), TimeZoneInterface {
    private val formatter = DateTimeFormatter.ofPattern("H[H]:m[m][:s[s]][.SSS][ zzz]")
    var default: String?
        get() {
            return defaultLocalTime?.toString()
        }
        set(value) {
            defaultLocalTime = value?.let {
                LocalTime.parse(it, formatter)
            }
        }
    var defaultDate: Date?
        set(value) {
            defaultLocalTime = value?.let {
                LocalDateTime.ofInstant(
                    it.toInstant(), ZoneId.systemDefault()
                ).toLocalTime()
            }
        }
        get() {
            return defaultLocalTime?.let {
                Date.from(
                    it
                        .atDate(LocalDate.now())
                        .atZone(ZoneId.systemDefault()).toInstant()
                )
            }
        }
    var defaultLocalTime: LocalTime? = null
        set(value) {
            field = value
            sqlDefault = value?.let { "'$it'" }
        }

    override var sqlDefault: String? = null

    override var withTimeZone = false
}
