package dev.alpas.ozone

import dev.alpas.exceptions.toHttpException
import dev.alpas.orAbort
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.database.DialectFeatureNotSupportedException
import me.liuwj.ktorm.dsl.AssignmentsBuilder
import me.liuwj.ktorm.dsl.Query
import me.liuwj.ktorm.dsl.insertAndGenerateKey
import me.liuwj.ktorm.dsl.limit
import me.liuwj.ktorm.entity.findById
import me.liuwj.ktorm.entity.findOne
import me.liuwj.ktorm.expression.SelectExpression
import me.liuwj.ktorm.expression.UnionExpression
import me.liuwj.ktorm.schema.BaseTable
import me.liuwj.ktorm.schema.ColumnDeclaring
import me.liuwj.ktorm.schema.SqlType
import org.eclipse.jetty.http.HttpStatus

/**
 * Checks whether this database is of the given name type.
 */
fun Database.isOfType(name: String): Boolean {
    return this.productName.toLowerCase() == name.toLowerCase()
}

/**
 * Checks whether this databse is of type MySQL.
 */
fun Database.isMySql(): Boolean {
    return isOfType("mysql")
}

/**
 * Checks whether this databse is of type SQLite.
 */
fun Database.isSqlite(): Boolean {
    return isOfType("sqlite")
}

/**
 * Obtain a entity object by its ID, auto left joining all the reference tables.
 *
 * This function will throw a NotFoundException if no records found, and throw an exception if there are more than one record.
 */
@Suppress("UNCHECKED_CAST")
fun <E : Any> BaseTable<E>.findOrFail(
    id: Any, message: String? = null,
    statusCode: Int = HttpStatus.NOT_FOUND_404
): E {
    return this.findById(id).orAbort(message ?: "Record with id $id doesn't exist.", statusCode)
}

fun <S : Any, T : BaseTable<S>> T.create(block: AssignmentsBuilder.(T) -> Unit): S {
    val id = this.insertAndGenerateKey(block)
    return this.findOrFail(id)
}

/**
 * Obtain a entity object matching the given [predicate], auto left joining all the reference tables.
 *
 * This function will abort if no records found, and throw an exception if there are more than one record.
 */
inline fun <E : Any, T : BaseTable<E>> T.findOneOrFail(
    message: String? = null,
    statusCode: Int = HttpStatus.NOT_FOUND_404,
    predicate: (T) -> ColumnDeclaring<Boolean>
): E {
    return this.findOne(predicate).orAbort(message, statusCode)
}

internal fun <T : Any> SqlType<T>.isVarChar(): Boolean = this.typeName.toLowerCase() == "varchar"

fun Query.selectFirst() = apply {
    try {
        limit(0, 1).firstOrNull()
    } catch (e: DialectFeatureNotSupportedException) {
        firstOrNull()
    }
}

internal fun Query.forUpdate(): Query {
    val expr = this.expression

    val exprForUpdate = when (expr) {
        is SelectExpression -> expr.copy(extraProperties = expr.extraProperties + Pair("forUpdate", true))
        is UnionExpression -> expr.copy(extraProperties = expr.extraProperties + Pair("forUpdate", true))
    }

    return this.copy(expression = exprForUpdate)
}
