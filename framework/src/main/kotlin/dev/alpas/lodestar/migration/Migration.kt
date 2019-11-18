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
import dev.alpas.lodestar.migration.table.column.TextColumn
import dev.alpas.lodestar.migration.table.column.TimeColumn
import dev.alpas.lodestar.migration.table.column.TimestampColumn
import dev.alpas.lodestar.migration.table.column.VarcharColumn
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Date

abstract class Migration {
    internal lateinit var connection: ConnectionInterface
    internal var tableNameIsInPluralForm = false
    internal lateinit var filename: String

    private val adapter by lazy { connection.adapter }

    fun createTable(name: String, block: TableBuilder.() -> Unit) {
        adapter.createTable(name) {
            this.tableNameIsInPluralForm = this@Migration.tableNameIsInPluralForm
            block()
        }
    }

    fun dropTable(name: String) {
        adapter.dropTable(name)
    }

    fun removeColumn(tableName: String, columnName: String) {
        adapter.removeColumn(tableName, columnName)
    }

    /**
     * Add new column to existing table
     *
     * @param first You add column at first of the column (valid only for MySQL)
     * @param justBeforeColumnName Column name the new column to be added just after.
     * valid only for MySQL.
     */
    private fun addColumn(
        tableName: String,
        column: AbstractColumn,
        first: Boolean = false,
        justBeforeColumnName: String? = null
    ) {
        val option = AddingColumnOption().also {
            it.first = first
            it.justBeforeColumn = justBeforeColumnName
        }
        adapter.addColumn(tableName, column, option)
    }

    /**
     * Add new integer column to existing table.
     *
     * @param tableName Table name.
     * @param columnName Column name.
     * @param nullable
     * @param default
     * @param unsigned Valid only for MySQL.
     * @param first You add column at first of the column (valid only for MySQL)
     * @param justBeforeColumnName Column name the new column to be added just after.
     * (valid only for MySQL)
     */
    fun addIntegerColumn(
        tableName: String, columnName: String,
        nullable: Boolean = true, default: Long? = null,
        unsigned: Boolean = false,
        first: Boolean = false,
        justBeforeColumnName: String? = null
    ) {
        val integerColumn = IntegerColumn(columnName)
        integerColumn.also {
            it.isNullable = nullable
            it.default = default
            it.unsigned = unsigned
        }
        addColumn(tableName, integerColumn, first, justBeforeColumnName)
    }

    /**
     * Add new integer column to existing table.
     *
     * @param tableName Table name.
     * @param columnName Column name.
     * @param nullable
     * @param default
     * @param unsigned Valid only for MySQL.
     * @param first You add column at first of the column (valid only for MySQL)
     * @param justBeforeColumnName Column name the new column to be added just after.
     * (valid only for MySQL)
     */
    fun addIntegerColumn(
        tableName: String, columnName: String,
        nullable: Boolean = true, default: RawSql,
        unsigned: Boolean = false,
        first: Boolean = false,
        justBeforeColumnName: String? = null
    ) {
        val integerColumn = IntegerColumn(columnName)
        integerColumn.also {
            it.isNullable = nullable
            it.sqlDefault = default.sql
            it.unsigned = unsigned
        }
        addColumn(tableName, integerColumn, first, justBeforeColumnName)
    }

    /**
     * Add new decimal column to existing table.
     *
     * @param first You add column at first of the columns (valid only for MySQL)
     * @param justBeforeColumnName Column name the new column to be add just after
     * (valid only for MySQL)
     */
    fun addDecimalColumn(
        tableName: String, columnName: String,
        precision: Int? = null, scale: Int? = null,
        nullable: Boolean = true, default: Double? = null,
        first: Boolean = false,
        justBeforeColumnName: String? = null
    ) {
        val decimalColumn = DecimalColumn(columnName).also {
            it.precision = precision
            it.scale = scale
            it.isNullable = nullable
            it.default = default
        }
        addColumn(tableName, decimalColumn, first, justBeforeColumnName)
    }

    /**
     * Add new decimal column to existing table.
     *
     * @param first You add column at first of the columns (valid only for MySQL)
     * @param justBeforeColumnName Column name the new column to be add just after
     * (valid only for MySQL)
     */
    fun addDecimalColumn(
        tableName: String, columnName: String,
        precision: Int? = null, scale: Int? = null,
        nullable: Boolean = true, default: RawSql,
        first: Boolean = false,
        justBeforeColumnName: String? = null
    ) {
        val decimalColumn = DecimalColumn(columnName).also {
            it.precision = precision
            it.scale = scale
            it.isNullable = nullable
            it.sqlDefault = default.sql
        }
        addColumn(tableName, decimalColumn, first, justBeforeColumnName)
    }

    /**
     * Add new varchar column to existing table.
     *
     * @param tableName
     * @param columnName
     * @param size
     * @param nullable
     * @param default
     * @param first You add column at first of the columns (valid only for MySQL)
     * @param justBeforeColumnName Column name the new column to be add just after
     * (valid only for MySQL)
     */
    fun addVarcharColumn(
        tableName: String, columnName: String, size: Int? = null,
        nullable: Boolean = true, default: String? = null,
        first: Boolean = false, justBeforeColumnName: String? = null
    ) {
        val varcharColumn = VarcharColumn(columnName)
        varcharColumn.also {
            it.size = size
            it.isNullable = nullable
            it.default = default
        }
        addColumn(tableName, varcharColumn, first, justBeforeColumnName)
    }

    /**
     * Add new varchar column to existing table.
     *
     * @param tableName
     * @param columnName
     * @param size
     * @param nullable
     * @param default
     * @param first You add column at first of the columns (valid only for MySQL)
     * @param justBeforeColumnName Column name the new column to be add just after
     * (valid only for MySQL)
     */
    fun addVarcharColumn(
        tableName: String, columnName: String, size: Int? = null,
        nullable: Boolean = true, default: RawSql,
        first: Boolean = false, justBeforeColumnName: String? = null
    ) {
        val varcharColumn = VarcharColumn(columnName)
        varcharColumn.also {
            it.size = size
            it.isNullable = nullable
            it.sqlDefault = default.sql
        }
        addColumn(tableName, varcharColumn, first, justBeforeColumnName)
    }

    /**
     * Add new boolean column to existing table.
     *
     * In PostgreSQL, BOOLEAN column will be added.
     * In MySQL, TINYINT column will be added.
     *
     * @param tableName
     * @param columnName
     * @param nullable
     * @param default
     * @param first You add column at first of the columns (valid only for MySQL)
     * @param justBeforeColumnName Column name the new column to be added just after.
     * (valid only for MySQL)
     */
    fun addBooleanColumn(
        tableName: String, columnName: String,
        nullable: Boolean = true, default: Boolean? = null,
        first: Boolean = false, justBeforeColumnName: String? = null
    ) {
        val booleanColumn = BooleanColumn(columnName)
        booleanColumn.also {
            it.isNullable = nullable
            it.default = default
        }
        addColumn(tableName, booleanColumn, first, justBeforeColumnName)
    }

    /**
     * Add new boolean column to existing table.
     *
     * In PostgreSQL, BOOLEAN column will be added.
     * In MySQL, TINYINT column will be added.
     *
     * @param tableName
     * @param columnName
     * @param nullable
     * @param default
     * @param first You add column at first of the columns (valid only for MySQL)
     * @param justBeforeColumnName Column name the new column to be added just after.
     * (valid only for MySQL)
     */
    fun addBooleanColumn(
        tableName: String, columnName: String,
        nullable: Boolean = true, default: RawSql,
        first: Boolean = false, justBeforeColumnName: String? = null
    ) {
        val booleanColumn = BooleanColumn(columnName)
        booleanColumn.also {
            it.isNullable = nullable
            it.sqlDefault = default.sql
        }
        addColumn(tableName, booleanColumn, first, justBeforeColumnName)
    }

    /**
     * Add new date column to existing table.
     *
     * @param tableName
     * @param columnName
     * @param nullable
     * @param default
     * @param first You add column at first of the column (valid only for MySQL)
     * @param justBeforeColumnName Column name the new column to be added just after.
     * valid only for MySQL
     */
    fun addDateColumn(
        tableName: String, columnName: String,
        nullable: Boolean = true, default: Date,
        first: Boolean = false, justBeforeColumnName: String? = null
    ) {
        val dateColumn = DateColumn(columnName).also {
            it.isNullable = nullable
            it.defaultDate = default
        }
        addColumn(tableName, dateColumn, first, justBeforeColumnName)
    }

    /**
     * Add new date column to existing table, with String default value.
     *
     * @param tableName
     * @param columnName
     * @param nullable
     * @param default Must be formatted as yyyy-MM-dd
     * @param first You add column at first of the column (valid only for MySQL)
     * @param justBeforeColumnName Column name the new column to be added just after.
     * (valid only for MySQL)
     */
    fun addDateColumn(
        tableName: String, columnName: String,
        nullable: Boolean = true, default: String? = null,
        first: Boolean = false, justBeforeColumnName: String? = null
    ) {
        val dateColumn = DateColumn(columnName).also {
            it.isNullable = nullable
            it.default = default
        }
        addColumn(tableName, dateColumn, first, justBeforeColumnName)
    }

    /**
     * Add new date column to existing table, with String default value.
     *
     * @param tableName
     * @param columnName
     * @param nullable
     * @param default
     * @param first You add column at first of the column (valid only for MySQL)
     * @param justBeforeColumnName Column name the new column to be added just after.
     * (valid only for MySQL)
     */
    fun addDateColumn(
        tableName: String, columnName: String,
        nullable: Boolean = true, default: LocalDate,
        first: Boolean = false, justBeforeColumnName: String? = null
    ) {
        val dateColumn = DateColumn(columnName).also {
            it.isNullable = nullable
            it.defaultLocalDate = default
        }
        addColumn(tableName, dateColumn, first, justBeforeColumnName)
    }

    /**
     * Add new date column to existing table.
     *
     * @param tableName
     * @param columnName
     * @param nullable
     * @param default
     * @param first You add column at first of the column (valid only for MySQL)
     * @param justBeforeColumnName Column name the new column to be added just after.
     * valid only for MySQL
     */
    fun addDateColumn(
        tableName: String, columnName: String,
        nullable: Boolean = true, default: RawSql,
        first: Boolean = false, justBeforeColumnName: String? = null
    ) {
        val dateColumn = DateColumn(columnName).also {
            it.isNullable = nullable
            it.sqlDefault = default.sql
        }
        addColumn(tableName, dateColumn, first, justBeforeColumnName)
    }

    /**
     * Add new text column to existing table.
     *
     * @param tableName
     * @param columnName
     * @param nullable
     * @param default
     * @param first You add column at first of the column (valid only for MySQL)
     * @param justBeforeColumnName Column name the new column to be added just after.
     * valid only for MySQL
     */
    fun addTextColumn(
        tableName: String, columnName: String,
        nullable: Boolean = true, default: String? = null,
        first: Boolean = false, justBeforeColumnName: String? = null
    ) {
        val textColumn = TextColumn(columnName)
        textColumn.also {
            it.isNullable = nullable
            it.default = default
        }
        addColumn(tableName, textColumn, first, justBeforeColumnName)
    }

    /**
     * Add new text column to existing table.
     *
     * @param tableName
     * @param columnName
     * @param nullable
     * @param default
     * @param first You add column at first of the column (valid only for MySQL)
     * @param justBeforeColumnName Column name the new column to be added just after.
     * valid only for MySQL
     */
    fun addTextColumn(
        tableName: String, columnName: String,
        nullable: Boolean = true, default: RawSql,
        first: Boolean = false, justBeforeColumnName: String? = null
    ) {
        val textColumn = TextColumn(columnName)
        textColumn.also {
            it.isNullable = nullable
            it.sqlDefault = default.sql
        }
        addColumn(tableName, textColumn, first, justBeforeColumnName)
    }

    /**
     * Add new BLOB column to existing table.
     *
     * ## PostgreSQL
     *
     * add BYTEA column instead, because PstgreSQL doesn't have BLOB type.
     *
     * @param tableName
     * @param columnName
     * @param nullable
     * @param default Not valid for MySQL
     * @param first You add column at first of the column (valid only for MySQL)
     * @param justBeforeColumnName Column name the new column to be added just after.
     * valid only for MySQL
     */
    fun addBlobColumn(
        tableName: String, columnName: String,
        nullable: Boolean = true, default: ByteArray? = null,
        first: Boolean = false, justBeforeColumnName: String? = null
    ) {
        val blobColumn = BlobColumn(columnName)
        blobColumn.also {
            it.isNullable = nullable
            it.default = default
        }
        addColumn(tableName, blobColumn, first, justBeforeColumnName)
    }

    /**
     * Add new BLOB column to existing table.
     *
     * ## PostgreSQL
     *
     * add BYTEA column instead, because PstgreSQL doesn't have BLOB type.
     *
     * @param tableName
     * @param columnName
     * @param nullable
     * @param default Not valid for MySQL
     * @param first You add column at first of the column (valid only for MySQL)
     * @param justBeforeColumnName Column name the new column to be added just after.
     * valid only for MySQL
     */
    fun addBlobColumn(
        tableName: String, columnName: String,
        nullable: Boolean = true, default: RawSql,
        first: Boolean = false, justBeforeColumnName: String? = null
    ) {
        val blobColumn = BlobColumn(columnName)
        blobColumn.also {
            it.isNullable = nullable
            it.sqlDefault = default.sql
        }
        addColumn(tableName, blobColumn, first, justBeforeColumnName)
    }

    /**
     * Add new TIME column to existing table.
     *
     * @param tableName
     * @param columnName
     * @param nullable
     * @param default
     * @param withTimeZone Valid only in PostgreSQL
     * @param first You add column at first of the column (valid only for MySQL)
     * @param justBeforeColumnName Column name the new column to be added just after.
     * valid only for MySQL
     */
    fun addTimeColumn(
        tableName: String, columnName: String,
        nullable: Boolean = false, default: LocalTime,
        withTimeZone: Boolean = false,
        first: Boolean = false, justBeforeColumnName: String? = null
    ) {
        val timeColumn = TimeColumn(columnName).also {
            it.isNullable = nullable
            it.defaultLocalTime = default
            it.withTimeZone = withTimeZone
        }
        addColumn(tableName, timeColumn, first, justBeforeColumnName)
    }

    /**
     * Add new TIME column to existing table.
     *
     * @param tableName
     * @param columnName
     * @param nullable
     * @param default Must be formatted as `HH:MM:ss.SSS`.
     * @param withTimeZone Valid only for PostgreSQL.
     * @param first You add column at first of the column (valid only for MySQL)
     * @param justBeforeColumnName Column name the new column to be added just after.
     * valid only for MySQL
     */
    fun addTimeColumn(
        tableName: String, columnName: String,
        nullable: Boolean = false, default: String? = null,
        withTimeZone: Boolean = false,
        first: Boolean = false, justBeforeColumnName: String? = null
    ) {
        val timeColumn = TimeColumn(columnName).also {
            it.isNullable = nullable
            it.default = default
            it.withTimeZone = withTimeZone
        }
        addColumn(tableName, timeColumn, first, justBeforeColumnName)
    }

    /**
     * Add new TIME column to existing table.
     *
     * @param tableName
     * @param columnName
     * @param nullable
     * @param default
     * @param withTimeZone Valid only for PostgreSQL
     * @param first You add column at first of the column (valid only for MySQL)
     * @param justBeforeColumnName Column name the new column to be added just after.
     * valid only for MySQL
     */
    fun addTimeColumn(
        tableName: String, columnName: String,
        nullable: Boolean = false, default: Date,
        withTimeZone: Boolean = false,
        first: Boolean = false, justBeforeColumnName: String? = null
    ) {
        val timeColumn = TimeColumn(columnName).also {
            it.isNullable = nullable
            it.defaultDate = default
            it.withTimeZone = withTimeZone
        }
        addColumn(tableName, timeColumn, first, justBeforeColumnName)
    }

    /**
     * Add new TIME column to existing table.
     *
     * @param tableName Table name.
     * @param columnName Column name.
     * @param nullable
     * @param default
     * @param withTimeZone Valid only for PostgreSQL
     * @param first You add column at first of the column (valid only for MySQL)
     * @param justBeforeColumnName Column name the new column to be added just after.
     * valid only for MySQL
     */
    fun addTimeColumn(
        tableName: String, columnName: String,
        nullable: Boolean = false, default: RawSql,
        withTimeZone: Boolean = false,
        first: Boolean = false, justBeforeColumnName: String? = null
    ) {
        val timeColumn = TimeColumn(columnName).also {
            it.isNullable = nullable
            it.sqlDefault = default.sql
            it.withTimeZone = withTimeZone
        }
        addColumn(tableName, timeColumn, first, justBeforeColumnName)
    }

    /**
     * Add new DATETIME column to existing table.
     *
     * ## PostgreSQL
     *
     * TIMESTAMP column will be added instead.
     *
     * @param tableName
     * @param columnName
     * @param nullable
     * @param default Must be formed like yyyy-MM-dd HH:MM:SS
     * @param first You add column at first of the column (valid only for MySQL)
     * @param justBeforeColumnName Column name the new column to be added just after.
     * valid only for MySQL
     */
    fun addDateTimeColumn(
        tableName: String, columnName: String,
        nullable: Boolean = false, default: String? = null,
        first: Boolean = false, justBeforeColumnName: String? = null
    ) {
        val dateTimeColumn = DateTimeColumn(columnName).also {
            it.isNullable = nullable
            it.default = default
        }
        addColumn(tableName, dateTimeColumn, first, justBeforeColumnName)
    }

    /**
     * Add new DATETIME column to existing table.
     *
     * In PostgreSQL, TIMESTAMP column will be added instead,
     * becuse PostgreSQL.doesn't have DATETIME type.
     *
     * @param tableName
     * @param columnName
     * @param nullable
     * @param default
     * @param first You add column at first of the column (valid only for MySQL)
     * @param justBeforeColumnName Column name the new column to be added just after.
     * valid only for MySQL
     */
    fun addDateTimeColumn(
        tableName: String, columnName: String,
        nullable: Boolean = false, default: Date,
        first: Boolean = false, justBeforeColumnName: String? = null
    ) {
        val dateTimeColumn = DateTimeColumn(columnName).also {
            it.isNullable = nullable
            it.defaultDate = default
        }
        addColumn(tableName, dateTimeColumn, first, justBeforeColumnName)
    }

    /**
     * Add new DATETIME column to existing table.
     *
     * In PostgreSQL, TIMESTAMP column will be added instead,
     * becuse PostgreSQL.doesn't have DATETIME type.
     *
     * @param tableName
     * @param columnName
     * @param nullable
     * @param default
     * @param first You add column at first of the column (valid only for MySQL)
     * @param justBeforeColumnName Column name the new column to be added just after.
     * valid only for MySQL
     */
    fun addDateTimeColumn(
        tableName: String, columnName: String,
        nullable: Boolean = false, default: LocalDateTime,
        first: Boolean = false, justBeforeColumnName: String? = null
    ) {
        val dateTimeColumn = DateTimeColumn(columnName).also {
            it.isNullable = nullable
            it.defaultLocalDateTime = default
        }
        addColumn(tableName, dateTimeColumn, first, justBeforeColumnName)
    }

    /**
     * Add new DATETIME column to existing table.
     *
     * In PostgreSQL, TIMESTAMP column will be added instead,
     * becuse PostgreSQL.doesn't have DATETIME type.
     *
     * @param tableName
     * @param columnName
     * @param nullable
     * @param default
     * @param first You add column at first of the column (valid only for MySQL)
     * @param justBeforeColumnName Column name the new column to be added just after.
     * valid only for MySQL
     */
    fun addDateTimeColumn(
        tableName: String, columnName: String,
        nullable: Boolean = false, default: RawSql,
        first: Boolean = false, justBeforeColumnName: String? = null
    ) {
        val dateTimeColumn = DateTimeColumn(columnName).also {
            it.isNullable = nullable
            it.sqlDefault = default.sql
        }
        addColumn(tableName, dateTimeColumn, first, justBeforeColumnName)
    }

    /**
     * Add new DATETIME column to existing table.
     *
     * @param tableName
     * @param columnName
     * @param nullable
     * @param default Must be formatted as `yyyy-MM-dd HH:mm:SS`.
     * @param withTimeZone Valid only in PostgreSQL
     * @param first You add column at first of the column (valid only for MySQL)
     * @param justBeforeColumnName Column name the new column to be added just after.
     * valid only for MySQL
     */
    fun addTimestampColumn(
        tableName: String, columnName: String,
        nullable: Boolean = false, default: String? = null,
        withTimeZone: Boolean = true,
        first: Boolean = false, justBeforeColumnName: String? = null
    ) {
        val timestampColumn = TimestampColumn(columnName).also {
            it.isNullable = nullable
            it.default = default
            it.withTimeZone = withTimeZone
        }
        addColumn(tableName, timestampColumn, first, justBeforeColumnName)
    }

    /**
     * Add new DATETIME column to existing table.
     *
     * @param tableName
     * @param columnName
     * @param nullable
     * @param withTimeZone Valid only in PostgreSQL
     * @param first You add column at first of the column (valid only for MySQL)
     * @param justBeforeColumnName Column name the new column to be added just after.
     * valid only for MySQL
     */
    fun addTimestampColumn(
        tableName: String, columnName: String,
        nullable: Boolean = false, default: Date,
        withTimeZone: Boolean = true,
        first: Boolean = false, justBeforeColumnName: String? = null
    ) {
        val timestampColumn = TimestampColumn(columnName).also {
            it.isNullable = nullable
            it.defaultDate = default
            it.withTimeZone = withTimeZone
        }
        addColumn(tableName, timestampColumn, first, justBeforeColumnName)
    }

    /**
     * Add new DATETIME column to existing table.
     *
     * @param tableName
     * @param columnName
     * @param nullable
     * @param default
     * @param withTimeZone Valid only in PostgreSQL.
     * @param first You add column at first of the column (valid only for MySQL)
     * @param justBeforeColumnName Column name the new column to be added just after.
     * valid only for MySQL
     */
    fun addTimestampColumn(
        tableName: String, columnName: String,
        nullable: Boolean = false, default: LocalDateTime,
        withTimeZone: Boolean = true,
        first: Boolean = false, justBeforeColumnName: String? = null
    ) {
        val timestampColumn = TimestampColumn(columnName).also {
            it.isNullable = nullable
            it.defaultLocalDateTime = default
            it.withTimeZone = withTimeZone
        }
        addColumn(tableName, timestampColumn, first, justBeforeColumnName)
    }

    /**
     * Add new DATETIME column to existing table.
     *
     * @param tableName
     * @param columnName
     * @param nullable
     * @param default
     * @param withTimeZone Valid only in PostgreSQL.
     * @param first You add column at first of the column (valid only for MySQL)
     * @param justBeforeColumnName Column name the new column to be added just after.
     * valid only for MySQL
     */
    fun addTimestampColumn(
        tableName: String, columnName: String,
        nullable: Boolean = false, default: RawSql,
        withTimeZone: Boolean = true,
        first: Boolean = false, justBeforeColumnName: String? = null
    ) {
        val timestampColumn = TimestampColumn(columnName).also {
            it.isNullable = nullable
            it.sqlDefault = default.sql
            it.withTimeZone = withTimeZone
        }
        addColumn(tableName, timestampColumn, first, justBeforeColumnName)
    }

    /**
     * Create Index
     *
     * @param tableName Table name.
     * @param columnName Column name.
     * @param unique `true` for unique index. The default value is `false`
     * @param method `null` means database default.
     */
    fun createIndex(tableName: String, columnName: String, unique: Boolean = false, method: IndexMethod? = null) {
        adapter.createIndex(tableName, arrayOf(columnName), unique, method)
    }

    /**
     * Create Index
     *
     * @param tableName Table name.
     * @param columnName Column name.
     * @param unique `true` for unique index. The default value is `false`
     * @param method `null` means database default.
     */
    fun createIndex(
        tableName: String,
        columnNameArray: Array<String>,
        unique: Boolean = false,
        method: IndexMethod? = null
    ) {
        adapter.createIndex(tableName, columnNameArray, unique, method)
    }

    /**
     * Create Index
     *
     * @param tableName Table name.
     * @param columnNameCollection Column name collection (List, Set).
     * @param unique `true` for unique index. The default value is `false`
     * @param method `null` means database default.
     */
    fun createIndex(
        tableName: String,
        columnNameCollection: Collection<String>,
        unique: Boolean = false,
        method: IndexMethod? = null
    ) = createIndex(tableName, columnNameCollection.toTypedArray(), unique, method)

    /**
     * Drop Index
     */
    fun dropIndex(tableName: String, indexName: String) {
        adapter.dropIndex(tableName, indexName)
    }

    /**
     * Rename table.
     *
     * @param oldName Old table name.
     * @param newName New table name.
     */
    fun renameTable(oldName: String, newName: String) {
        adapter.renameTable(oldName, newName)
    }

    /**
     * Rename column
     *
     * For MySQL, this can be used from version 8.0.
     *
     * @param tableName Table name
     * @param oldColumnName Old column name
     * @param newColumnName New column name
     */
    fun renameColumn(tableName: String, oldColumnName: String, newColumnName: String) {
        adapter.renameColumn(tableName, oldColumnName, newColumnName)
    }

    /**
     * Rename index.
     *
     * @param tableName Table name.
     * @param oldIndexName Old index name
     * @param newIndexName New index name
     */
    fun renameIndex(tableName: String, oldIndexName: String, newIndexName: String) {
        adapter.renameIndex(tableName, oldIndexName, newIndexName)
    }

    /**
     * Add foreign key constraint.
     *
     * @param tableName
     * @param columnName
     * @param referencedTableName
     * @param referencedColumnName
     */
    fun addForeignKey(
        tableName: String,
        columnName: String,
        referencedTableName: String,
        referencedColumnName: String = "id"
    ) {
        adapter.addForeignKey(tableName, columnName, referencedTableName, referencedColumnName)
    }

    /**
     * Drop foreign key constraint.
     *
     * @param tableName
     * @param columnName
     * @param keyConstraintName Foreign key constraint name.
     */
    fun dropForeignKey(tableName: String, columnName: String, keyConstraintName: String) {
        adapter.dropForeignKey(tableName, columnName, keyConstraintName)
    }

    /**
     * Drop foreign key constraint.
     *
     * @param tableName
     * @param columnName
     */
    fun dropForeignKey(tableName: String, columnName: String) {
        adapter.dropForeignKey(tableName, columnName)
    }

    /**
     * Execute SQL
     *
     * @param sql
     */
    fun executeSql(sql: String) {
        connection.execute(sql)
    }

    /**
     * Migrate up
     */
    open fun up() {}

    /**
     * Migrate down
     */
    open fun down() {}
}
