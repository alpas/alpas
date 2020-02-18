package dev.alpas.ozone

import me.liuwj.ktorm.schema.*
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

/**
 * Define a column typed of [LongSqlType] which is also an autoincrementing unsigned primary key.
 */
fun <E : OzoneEntity<E>> OzoneTable<E>.bigIncrements(name: String = "id"): BaseTable<E>.ColumnRegistration<Long> {
    return incrementsColumn(name, LongSqlType)
}

/**
 * Define a column typed of [IntSqlType] which is also an autoincrementing unsigned primary key.
 */
fun <E : OzoneEntity<E>> OzoneTable<E>.increments(name: String = "id"): BaseTable<E>.ColumnRegistration<Int> {
    return incrementsColumn(name, IntSqlType)
}

private fun <E : OzoneEntity<E>, C : Number> OzoneTable<E>.incrementsColumn(name: String, sqlType: SqlType<C>)
        : BaseTable<E>.ColumnRegistration<C> {
    return registerAndBind(name, sqlType).also {
        it.primaryKey()
        it.unsigned()
        it.autoIncrement()
    }
}

/**
 * Define a column typed of [VarcharSqlType] that accepts a size that is set to 255 by default.
 */
fun <E : OzoneEntity<E>> OzoneTable<E>.string(name: String, size: Int = 255): BaseTable<E>.ColumnRegistration<String> {
    return registerColumn(name, VarcharSqlType).apply {
        size(size)
    }
}

/**
 * Define a column typed of [VarcharSqlType] that accepts a size. The size won't be set if it is null.
 */
fun <E : OzoneEntity<E>> OzoneTable<E>.char(name: String, size: Int? = null): BaseTable<E>.ColumnRegistration<String> {
    return registerColumn(name, CharSqlType).apply {
        if (size != null) {
            size(size)
        }
    }
}

/**
 * Define a column typed of [CharSqlType].
 */
fun <E : Any> BaseTable<E>.char(name: String): BaseTable<E>.ColumnRegistration<String> {
    return registerColumn(name, CharSqlType)
}

/**
 * [SqlType] implementation represents `char` SQL type.
 */
object CharSqlType : SqlType<String>(Types.CHAR, "char") {
    override fun doSetParameter(ps: PreparedStatement, index: Int, parameter: String) {
        ps.setString(index, parameter)
    }

    override fun doGetResult(rs: ResultSet, index: Int): String? {
        return rs.getString(index)
    }
}

/**
 * Define a column typed of [TinyIntSqlType].
 */
fun <E : Any> BaseTable<E>.tinyInt(name: String): BaseTable<E>.ColumnRegistration<Int> {
    return registerColumn(name, TinyIntSqlType)
}

/**
 * [SqlType] implementation represents `tinyInt` SQL type.
 */
object TinyIntSqlType : SqlType<Int>(Types.TINYINT, "tinyInt") {
    override fun doSetParameter(ps: PreparedStatement, index: Int, parameter: Int) {
        ps.setInt(index, parameter)
    }

    override fun doGetResult(rs: ResultSet, index: Int): Int? {
        return rs.getInt(index)
    }
}

/**
 * Define a column typed of [SmallIntSqlType].
 */
fun <E : Any> BaseTable<E>.smallInt(name: String): BaseTable<E>.ColumnRegistration<Int> {
    return registerColumn(name, SmallIntSqlType)
}

/**
 * [SqlType] implementation represents `smalInt` SQL type.
 */
object SmallIntSqlType : SqlType<Int>(Types.SMALLINT, "smallInt") {
    override fun doSetParameter(ps: PreparedStatement, index: Int, parameter: Int) {
        ps.setInt(index, parameter)
    }

    override fun doGetResult(rs: ResultSet, index: Int): Int? {
        return rs.getInt(index)
    }
}

/**
 * Define a column typed of [LongSqlType].
 */
fun <E : Any> BaseTable<E>.bigInt(name: String): BaseTable<E>.ColumnRegistration<Long> {
    return registerColumn(name, LongSqlType)
}

/**
 * Define a column typed of [MediumTextSqlType].
 */
fun <E : Any> BaseTable<E>.mediumText(name: String): BaseTable<E>.ColumnRegistration<String> {
    return registerColumn(name, MediumTextSqlType)
}

/**
 * [SqlType] implementation represents `mediumText` SQL type.
 */
object MediumTextSqlType : SqlType<String>(Types.LONGVARCHAR, "mediumText") {
    override fun doSetParameter(ps: PreparedStatement, index: Int, parameter: String) {
        ps.setString(index, parameter)
    }

    override fun doGetResult(rs: ResultSet, index: Int): String? {
        return rs.getString(index)
    }
}

/**
 * Define a column typed of [LongTextSqlType].
 */
fun <E : Any> BaseTable<E>.longText(name: String): BaseTable<E>.ColumnRegistration<String> {
    return registerColumn(name, LongTextSqlType)
}

/**
 * [SqlType] implementation represents `longText` SQL type.
 */
object LongTextSqlType : SqlType<String>(Types.LONGVARCHAR, "longText") {
    override fun doSetParameter(ps: PreparedStatement, index: Int, parameter: String) {
        ps.setString(index, parameter)
    }

    override fun doGetResult(rs: ResultSet, index: Int): String? {
        return rs.getString(index)
    }
}
