package dev.alpas.ozone.validation

import dev.alpas.http.HttpCall
import dev.alpas.validation.ErrorMessage
import dev.alpas.validation.Rule
import dev.alpas.validation.ValidationGuard
import org.jetbrains.exposed.sql.transactions.transaction

class Unique(
    private val table: String,
    private val column: String? = null,
    private val ignore: String? = null,
    private val message: ErrorMessage = null
) : Rule() {
    override fun check(attribute: String, call: HttpCall): Boolean {
        val value = call.param(attribute)
        val exists = transaction {
            val columnToCheck = column ?: attribute
            var query = "SELECT COUNT(1) FROM `$table` WHERE `$columnToCheck` = ?"
            val ignoreProps = ignore?.split(":") ?: emptyList()
            val shouldIgnoreField = ignoreProps.count() == 2
            if (shouldIgnoreField) {
                query = "$query AND `${ignoreProps[0].trim()}` != ?"
            }
            val statement = connection.prepareStatement(query, false)
            statement[1] = value
            if (shouldIgnoreField) {
                statement[2] = ignoreProps[1].trim()
            }
            statement.executeQuery().let { result ->
                result.next()
                result.getBoolean(1)
            }
        }
        return !exists.also { fails ->
            if (fails) {
                error = message?.let { it(attribute, value) } ?: "$attribute already exists"
            }
        }
    }
}

fun ValidationGuard.unique(
    table: String,
    column: String? = null,
    ignore: String? = null,
    message: ErrorMessage = null
): Rule {
    return rule(Unique(table, column, ignore, message))
}
