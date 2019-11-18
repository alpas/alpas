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

package dev.alpas.lodestar.migration

import dev.alpas.lodestar.DatabaseConfig
import dev.alpas.lodestar.migration.adapter.DbAdapter
import dev.alpas.lodestar.migration.adapter.MySqlAdapter
import dev.alpas.lodestar.migration.adapter.PostgreSqlAdapter
import dev.alpas.lodestar.migration.adapter.SqliteAdapter
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.database.useConnection

internal class Connection(dbConfig: DatabaseConfig, override val isDryRun: Boolean = false) : ConnectionInterface {
    private val db by lazy { dbConfig.connect() }
    override val adapter: DbAdapter by lazy {
        when {
            db.isOfType("sqlite") -> SqliteAdapter(this)
            db.isOfType("mysql") -> MySqlAdapter(this)
            else -> PostgreSqlAdapter(this)
        }.also {
            // it.dispSql = dispSql
            // it.isReview = isReview
        }
    }

    override fun execute(sql: String): Boolean {
        useConnection {
            it.prepareStatement(sql).use { statement ->
                return statement.execute()
            }
        }
    }

    override fun doesTableExist(tableName: String): Boolean {
        useConnection {
            val table = if (db.isOfType("oracle")) tableName.toUpperCase() else tableName
            return it.metaData.getTables(null, null, table, null).use { resultset ->
                resultset.next()
            }
        }
    }
}

private fun Database.isOfType(name: String): Boolean {
    return this.productName.toLowerCase() == name.toLowerCase()
}
