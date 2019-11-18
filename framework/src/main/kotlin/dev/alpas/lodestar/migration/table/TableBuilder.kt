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

package dev.alpas.lodestar.migration.table

import com.cesarferreira.pluralize.singularize
import dev.alpas.lodestar.migration.RawSql
import dev.alpas.lodestar.migration.table.column.AbstractColumn
import dev.alpas.lodestar.migration.table.column.AutoIncrementColumn
import dev.alpas.lodestar.migration.table.column.BigIntegerColumn
import dev.alpas.lodestar.migration.table.column.BlobColumn
import dev.alpas.lodestar.migration.table.column.BooleanColumn
import dev.alpas.lodestar.migration.table.column.ColumnBuilder
import dev.alpas.lodestar.migration.table.column.DateColumn
import dev.alpas.lodestar.migration.table.column.DateTimeColumn
import dev.alpas.lodestar.migration.table.column.DecimalColumn
import dev.alpas.lodestar.migration.table.column.IntegerColumn
import dev.alpas.lodestar.migration.table.column.JsonColumn
import dev.alpas.lodestar.migration.table.column.TextColumn
import dev.alpas.lodestar.migration.table.column.TimeColumn
import dev.alpas.lodestar.migration.table.column.TimestampColumn
import dev.alpas.lodestar.migration.table.column.VarcharColumn
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Date

class TableBuilder {
    lateinit var tableName: String
    internal val columnList = mutableListOf<AbstractColumn>()
    internal var comment: String? = null
    /** Specify table name pluralization */
    var tableNameIsInPluralForm = false

    private fun addColumn(column: AbstractColumn) {
        columnList.add(column)
    }

    /**
     * Add an auto increment unsigned integer primary key column.
     *
     * @param columnName
     * @return
     */
    fun increments(columnName: String): ColumnBuilder {
        val builder = ColumnBuilder(AutoIncrementColumn(columnName))
        addColumn(builder.column)
        return builder
    }

    /**
     * Add an auto increment unsigned big integer primary key column.
     *
     * @param columnName
     * @return
     */
    fun bigIncrements(columnName: String): ColumnBuilder {
        val builder = ColumnBuilder(AutoIncrementColumn(columnName).also { it.isBigInt = true })
        addColumn(builder.column)
        return builder
    }

    /**
     * Add decimal column
     *
     * @param columnName
     * @param precision The number of digits in the number.
     * @param scale The number of digits to the right of the decimal point in the number.
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default
     * @return
     */
    fun decimal(
        columnName: String,
        precision: Int? = null,
        scale: Int? = null,
        nullable: Boolean = false,
        default: Double? = null
    ): ColumnBuilder {
        val decimalColumn = DecimalColumn(columnName).also {
            it.isNullable = nullable
            it.default = default
            it.precision = precision
            it.scale = scale
        }
        addColumn(decimalColumn)
        return ColumnBuilder(decimalColumn)
    }

    /**
     * Add decimal column
     *
     * @param columnName
     * @param precision The number of digits in the number.
     * @param scale The number of digits to the right of the decimal point in the number.
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default
     * @return
     */
    fun decimal(
        columnName: String,
        precision: Int? = null,
        scale: Int? = null,
        nullable: Boolean = false,
        default: RawSql
    ): ColumnBuilder {
        val decimalColumn = DecimalColumn(columnName).also {
            it.isNullable = nullable
            it.sqlDefault = default.sql
            it.precision = precision
            it.scale = scale
        }
        addColumn(decimalColumn)
        return ColumnBuilder(decimalColumn)
    }

    /**
     * Add integer column.
     *
     * @param columnName
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default
     * @param unsigned Valid only for MySQL.
     * @return
     */
    fun integer(
        columnName: String,
        nullable: Boolean = false,
        default: Long? = null,
        unsigned: Boolean = false
    ): ColumnBuilder {
        val builder = ColumnBuilder(IntegerColumn(columnName).also {
            it.isNullable = nullable
            it.default = default
            it.unsigned = unsigned
        })
        addColumn(builder.column)
        return builder
    }

    /**
     * Add a big integer column.
     *
     * @param columnName
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default
     * @param unsigned Valid only for MySQL.
     * @return
     */
    fun bigInteger(
        columnName: String,
        nullable: Boolean = false,
        default: Long? = null,
        unsigned: Boolean = false
    ): ColumnBuilder {
        val builder = ColumnBuilder(BigIntegerColumn(columnName).also {
            it.isNullable = nullable
            it.default = default
            it.unsigned = unsigned
        })
        addColumn(builder.column)
        return builder
    }

    /**
     * Add integer column.
     *
     * @param columnName
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default
     * @param unsigned Valid only for MySQL.
     * @return
     */
    fun integer(
        columnName: String,
        nullable: Boolean = false,
        default: RawSql,
        unsigned: Boolean = false
    ): ColumnBuilder {
        val builder = ColumnBuilder(IntegerColumn(columnName).also {
            it.isNullable = nullable
            it.sqlDefault = default.sql
            it.unsigned = unsigned
        })
        addColumn(builder.column)
        return builder
    }

    /**
     * Add unsigned integer column.
     *
     * @param columnName
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default
     * @return
     */
    fun unsignedInteger(
        columnName: String,
        nullable: Boolean = false,
        default: Long? = null
    ): ColumnBuilder {
        return integer(columnName, nullable, default, true)
    }

    /**
     * Add unsigned integer column. Valid only for MySQL
     *
     * @param columnName
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default
     * @return
     */
    fun unsignedInteger(
        columnName: String,
        nullable: Boolean = false,
        default: RawSql
    ): ColumnBuilder {
        return integer(columnName, nullable, default, true)
    }

    /**
     * add varchar column
     *
     * variable with limit
     *
     * @param columnName
     * @param size For MySQL, `null` means 255.
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default
     * @return
     */
    fun varchar(
        columnName: String,
        size: Int? = null,
        nullable: Boolean = false,
        default: String? = null
    ): ColumnBuilder {
        val builder = ColumnBuilder(VarcharColumn(columnName).also {
            it.isNullable = nullable
            it.default = default
            it.size = size
        })
        addColumn(builder.column)
        return builder
    }

    /**
     * add varchar column
     *
     * variable with limit
     *
     * @param columnName
     * @param size For MySQL, `null` means 255.
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default
     * @return
     */
    fun varchar(
        columnName: String,
        size: Int? = null,
        nullable: Boolean = false,
        default: RawSql
    ): ColumnBuilder {
        val builder = ColumnBuilder(VarcharColumn(columnName).also {
            it.isNullable = nullable
            it.sqlDefault = default.sql
            it.size = size
        })
        addColumn(builder.column)
        return builder
    }

    /**
     * add varchar column
     *
     * Alias for varchar.
     *
     * @param columnName
     * @param size
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default
     * @return
     */
    fun string(
        columnName: String,
        size: Int? = null,
        nullable: Boolean = false,
        default: String? = null
    ): ColumnBuilder {
        return varchar(columnName, size, nullable, default)
    }

    /**
     * add varchar column
     *
     * Alias for varchar.
     *
     * @param columnName
     * @param size
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default
     * @return
     */
    fun string(
        columnName: String,
        size: Int? = null,
        nullable: Boolean = false,
        default: RawSql
    ): ColumnBuilder {
        return varchar(columnName, size, nullable, default)
    }

    /**
     * add boolean column
     *
     * @param columnName
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default
     * @return
     */
    fun boolean(
        columnName: String,
        nullable: Boolean = false,
        default: Boolean? = null
    ): ColumnBuilder {
        val builder = ColumnBuilder(BooleanColumn(columnName).also {
            it.isNullable = nullable
            it.default = default
        })
        addColumn(builder.column)
        return builder
    }

    /**
     * add boolean column
     *
     * @param columnName
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default
     * @return
     */
    fun boolean(
        columnName: String,
        nullable: Boolean = false,
        default: RawSql
    ): ColumnBuilder {
        val builder = ColumnBuilder(BooleanColumn(columnName).also {
            it.isNullable = nullable
            it.sqlDefault = default.sql
        })
        addColumn(builder.column)
        return builder
    }

    /**
     * add date column of `java.util.Date` default value.
     *
     * @param columnName
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default
     * @return
     */
    fun date(
        columnName: String,
        nullable: Boolean = false,
        default: Date
    ): ColumnBuilder {
        val builder = ColumnBuilder(DateColumn(columnName).also {
            it.isNullable = nullable
            it.defaultDate = default
        })
        addColumn(builder.column)
        return builder
    }

    /**
     * add date column of no default value or `String` default value
     *
     * @param columnName
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default Must be formatted as yyyy-MM-dd
     * @return
     */
    fun date(
        columnName: String,
        nullable: Boolean = false,
        default: String? = null
    ): ColumnBuilder {
        val builder = ColumnBuilder(DateColumn(columnName).also {
            it.isNullable = nullable
            it.default = default
        })
        addColumn(builder.column)
        return builder
    }

    /**
     * add date column of `java.time.LocalDate` default value
     *
     * @param columnName
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default
     * @return
     */
    fun date(
        columnName: String,
        nullable: Boolean = false,
        default: LocalDate
    ): ColumnBuilder {
        val builder = ColumnBuilder(DateColumn(columnName).also {
            it.isNullable = nullable
            it.defaultLocalDate = default
        })
        addColumn(builder.column)
        return builder
    }

    /**
     * add date column of `RawSql` default value
     *
     * @param columnName
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default
     * @return
     */
    fun date(
        columnName: String,
        nullable: Boolean = false,
        default: RawSql
    ): ColumnBuilder {
        val builder = ColumnBuilder(DateColumn(columnName).also {
            it.isNullable = nullable
            it.sqlDefault = default.sql
        })
        addColumn(builder.column)
        return builder
    }

    /**
     * Add TEXT column, unlimited length string
     *
     * @param columnName
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default Invalid for MySQL
     * @return
     */
    fun text(
        columnName: String,
        nullable: Boolean = false,
        default: String? = null
    ): ColumnBuilder {
        val builder = ColumnBuilder(TextColumn(columnName).also {
            it.isNullable = nullable
            it.default = default
        })
        addColumn(builder.column)
        return builder
    }

    /**
     * Add JSON column
     *
     * @param columnName
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default Invalid for MySQL
     * @return
     */
    fun json(
        columnName: String,
        nullable: Boolean = false,
        default: String? = null
    ): ColumnBuilder {
        val builder = ColumnBuilder(JsonColumn(columnName).also {
            it.isNullable = nullable
            it.default = default
        })
        addColumn(builder.column)
        return builder
    }

    /**
     * Add TEXT column, unlimited length string
     *
     * @param columnName
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default Invalid for MySQL
     * @return
     */
    fun text(
        columnName: String,
        nullable: Boolean = false,
        default: RawSql
    ): ColumnBuilder {
        val builder = ColumnBuilder(TextColumn(columnName).also {
            it.isNullable = nullable
            it.sqlDefault = default.sql
        })
        addColumn(builder.column)
        return builder
    }

    /**
     * Add BLOB column
     *
     * ## PostgreSQL
     *
     * Add BYTEA column instead, because PostgreSQL doesn't have BLOB type.
     *
     * @param columnName
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default Invalid for MySQL
     * @return
     */
    fun blob(
        columnName: String,
        nullable: Boolean = false,
        default: ByteArray? = null
    ): ColumnBuilder {
        val builder = ColumnBuilder(BlobColumn(columnName).also {
            it.isNullable = nullable
            it.default = default
        })
        addColumn(builder.column)
        return builder
    }

    /**
     * Add BLOB column
     *
     * ## PostgreSQL
     *
     * Add BYTEA column instead, because PostgreSQL doesn't have BLOB type.
     *
     * @param columnName
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default Invalid for MySQL
     * @return
     */
    fun blob(
        columnName: String,
        nullable: Boolean = false,
        default: RawSql
    ): ColumnBuilder {
        val builder = ColumnBuilder(BlobColumn(columnName).also {
            it.isNullable = nullable
            it.sqlDefault = default.sql
        })
        addColumn(builder.column)
        return builder
    }

    /**
     * add Time column
     *
     * @param columnName
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default
     * @param withTimeZone Valid only for PostgreSQL.
     * @return
     */
    fun time(
        columnName: String,
        nullable: Boolean = false,
        default: LocalTime? = null,
        withTimeZone: Boolean = false
    ): ColumnBuilder {
        val builder = ColumnBuilder(TimeColumn(columnName).also {
            it.isNullable = nullable
            it.defaultLocalTime = default
            it.withTimeZone = withTimeZone
        })
        addColumn(builder.column)
        return builder
    }

    /**
     * add Time column
     *
     * @param columnName
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default Format as HH:mm:ss[.SSS][ zzz].
     * @param withTimeZone Valid only for PostgreSQL.
     * `22:21:22.123`, `22:21:22`, `12:23:34` can be accepted.
     * @return
     */
    fun time(
        columnName: String,
        nullable: Boolean = false,
        default: String,
        withTimeZone: Boolean = false
    ): ColumnBuilder {
        val builder = ColumnBuilder(TimeColumn(columnName).also {
            it.isNullable = nullable
            it.default = default
            it.withTimeZone = withTimeZone
        })
        addColumn(builder.column)
        return builder
    }

    /**
     * add Time column
     *
     * @param columnName
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default
     * @param withTimeZone Valid only for PostgreSQL.
     * @return
     */
    fun time(
        columnName: String,
        nullable: Boolean = false,
        default: Date,
        withTimeZone: Boolean = false
    ): ColumnBuilder {
        val builder = ColumnBuilder(TimeColumn(columnName).also {
            it.isNullable = nullable
            it.defaultDate = default
            it.withTimeZone = withTimeZone
        })
        addColumn(builder.column)
        return builder
    }

    /**
     * add Time column
     *
     * @param columnName
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default
     * @param withTimeZone Valid only for PostgreSQL.
     * @return
     */
    fun time(
        columnName: String,
        nullable: Boolean = false,
        default: RawSql,
        withTimeZone: Boolean = false
    ): ColumnBuilder {
        val builder = ColumnBuilder(TimeColumn(columnName).also {
            it.isNullable = nullable
            it.sqlDefault = default.sql
            it.withTimeZone = withTimeZone
        })
        addColumn(builder.column)
        return builder
    }

    /**
     * add TIMESTAMP column
     *
     * ## MySQL
     *
     * If you store the value,
     * and then change the time zone and retrieve the value,
     * the retrieved value is different from the value you stored.
     *
     * @param columnName
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default
     * @param withTimeZone Valid only for PostgreSQL.
     * @return
     */
    fun timestamp(
        columnName: String,
        default: String? = null,
        withTimeZone: Boolean = false
    ): ColumnBuilder {
        val builder = ColumnBuilder(TimestampColumn(columnName).also {
            it.default = default
            it.withTimeZone = withTimeZone
        })
        addColumn(builder.column)
        return builder
    }

    /**
     * add TIMESTAMP column
     *
     * ## MySQL
     *
     * If you store the value,
     * and then change the time zone and retrieve the value,
     * the retrieved value is different from the value you stored.
     *
     * @param columnName
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default
     * @param withTimeZone Valid only for PostgreSQL.
     * @return
     */
    fun timestamp(
        columnName: String,
        nullable: Boolean = false,
        default: Date,
        withTimeZone: Boolean = false
    ): ColumnBuilder {
        val builder = ColumnBuilder(TimestampColumn(columnName).also {
            it.isNullable = nullable
            it.defaultDate = default
            it.withTimeZone = withTimeZone
        })
        addColumn(builder.column)
        return builder
    }

    /**
     * add TIMESTAMP column
     *
     * ## MySQL
     *
     * If you store the value,
     * and then change the time zone and retrieve the value,
     * the retrieved value is different from the value you stored.
     *
     * @param columnName
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default
     * @param withTimeZone Valid only for PostgreSQL.
     * @return
     */
    fun timestamp(
        columnName: String,
        nullable: Boolean = false,
        default: LocalDateTime,
        withTimeZone: Boolean = false
    ): ColumnBuilder {
        val builder = ColumnBuilder(TimestampColumn(columnName).also {
            it.isNullable = nullable
            it.defaultLocalDateTime = default
            it.withTimeZone = withTimeZone
        })
        addColumn(builder.column)
        return builder
    }

    /**
     * add TIMESTAMP column
     *
     * ## MySQL
     *
     * If you store the value,
     * and then change the time zone and retrieve the value,
     * the retrieved value is different from the value you stored.
     *
     * @param columnName
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default
     * @param withTimeZone Valid only for PostgreSQL.
     * @return
     */
    fun timestamp(
        columnName: String,
        nullable: Boolean = false,
        default: RawSql,
        withTimeZone: Boolean = false
    ): ColumnBuilder {
        val builder = ColumnBuilder(TimestampColumn(columnName).also {
            it.isNullable = nullable
            it.sqlDefault = default.sql
            it.withTimeZone = withTimeZone
        })
        addColumn(builder.column)
        return builder
    }

    /**
     * add DATETIME column
     *
     * ## PostgreSQL
     *
     * There is no `DATETIME` type, so add `TIMESTAMP` column, instead.
     *
     * @param columnName
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default
     * @return
     */
    fun dateTime(
        columnName: String,
        nullable: Boolean = false,
        default: Date
    ): ColumnBuilder {
        val builder = ColumnBuilder(DateTimeColumn(columnName).also {
            it.isNullable = nullable
            it.defaultDate = default
        })
        addColumn(builder.build())
        return builder
    }

    /**
     * add DATETIME column
     *
     * ## PostgreSQL
     *
     * There is no `DATETIME` type, so add `TIMESTAMP` column, instead.
     *
     * @param columnName
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default
     * @return
     */
    fun dateTime(
        columnName: String,
        nullable: Boolean = false,
        default: String? = null
    ): ColumnBuilder {
        val builder = ColumnBuilder(DateTimeColumn(columnName).also {
            it.isNullable = nullable
            it.default = default
        })
        addColumn(builder.build())
        return builder
    }

    /**
     * add DATETIME column
     *
     * ## PostgreSQL
     *
     * There is no `DATETIME` type, so add `TIMESTAMP` column, instead.
     *
     * @param columnName
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default
     */
    fun dateTime(
        columnName: String,
        nullable: Boolean = false,
        default: LocalDateTime
    ): ColumnBuilder {
        val builder = ColumnBuilder(DateTimeColumn(columnName).also {
            it.isNullable = nullable
            it.defaultLocalDateTime = default
        })
        addColumn(builder.build())
        return builder
    }

    /**
     * add DATETIME column
     *
     * ## PostgreSQL
     *
     * There is no `DATETIME` type, so add `TIMESTAMP` column, instead.
     *
     * @param columnName
     * @param nullable `false` for `NOT NULL` constraint. The default value is `false`.
     * @param default
     */
    fun dateTime(
        columnName: String,
        nullable: Boolean = false,
        default: RawSql
    ): ColumnBuilder {
        val builder = ColumnBuilder(DateTimeColumn(columnName).also {
            it.isNullable = nullable
            it.sqlDefault = default.sql
        })
        addColumn(builder.build())
        return builder
    }

    /**
     * Add reference column.
     *
     * Create referenced column relating to `${name}_id` column.
     *
     * @param tableName Table name in most cases.
     * Column `${name}_id` will be created.
     * @param nullable
     * @param default
     * @param columnName
     * @return
     */
    private fun refer(
        tableName: String,
        nullable: Boolean = false,
        default: Long? = null,
        columnName: String = "id"
    ): ColumnBuilder {
        val modifiedTableName = if (tableNameIsInPluralForm) tableName.singularize() else tableName

        val builder =
            integer(
                modifiedTableName + "_" + columnName,
                nullable = nullable,
                default = default,
                unsigned = true
            )
        builder.refer(tableName, columnName)
        return builder
    }

    /**
     * Add an integer reference column.
     *
     *
     * @param foreignKey The foreign key of this table
     * @param nullable
     * @param default
     * @param tableToRefer The foreign table to refer to
     * @param columnToRefer The column to refer to on the foreign table
     * @param onDelete The on delete behavior
     * @return
     */
    fun integerReference(
        foreignKey: String,
        tableToRefer: String,
        columnToRefer: String = "id",
        nullable: Boolean = false,
        default: Long? = null,
        onDelete: String? = null
    ): ColumnBuilder {
        val builder = integer(foreignKey, nullable = nullable, default = default, unsigned = true)
        builder.refer(tableToRefer, columnToRefer, onDelete)
        return builder
    }

    /**
     * Add a big integer reference column.
     *
     *
     * @param foreignKey The foreign key of this table
     * @param nullable
     * @param default
     * @param tableToRefer The foreign table to refer to
     * @param columnToRefer The column to refer to on the foreign table
     * @param onDelete The on delete behavior
     * @return
     */
    fun bigIntegerReference(
        foreignKey: String,
        tableToRefer: String,
        columnToRefer: String = "id",
        nullable: Boolean = false,
        default: Long? = null,
        onDelete: String? = null
    ): ColumnBuilder {
        val builder = bigInteger(foreignKey, nullable = nullable, default = default, unsigned = true)
        builder.refer(tableToRefer, columnToRefer, onDelete)
        return builder
    }

    /**
     * Add table comment
     *
     * @param text Table comment.
     */
    fun comment(text: String) {
        comment = text
    }

    /**
     * add created_at and updated_at datetime columns
     *
     * @param withTimeZone Valid only for PostgreSQL.
     * @return
     */

    fun timestamps(withTimeZone: Boolean = false): ColumnBuilder {
        timestamp("created_at", withTimeZone = withTimeZone).nullable()
        return timestamp("updated_at", withTimeZone = withTimeZone).nullable()
    }
}
