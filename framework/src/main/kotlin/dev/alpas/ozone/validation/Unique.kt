package dev.alpas.ozone.validation

import dev.alpas.http.HttpCall
import dev.alpas.validation.ErrorMessage
import dev.alpas.validation.Rule
import dev.alpas.validation.ValidationGuard
import me.liuwj.ktorm.database.useConnection

class Unique(
    private val table: String,
    private val column: String? = null,
    private val ignore: String? = null,
    private val message: ErrorMessage = null
) : Rule() {
    override fun check(attribute: String, call: HttpCall): Boolean {
        val value = call.param(attribute)
        val exists = useConnection {
            val columnToCheck = column ?: attribute
            var query = "SELECT COUNT(1) FROM `$table` WHERE `$columnToCheck` = ?"
            val ignoreProps = ignore?.split(":") ?: emptyList()
            val shouldIgnoreField = ignoreProps.count() == 2
            if (shouldIgnoreField) {
                query = "$query AND `${ignoreProps[0].trim()}` != ?"
            }
            val statement = it.prepareStatement(query)
            statement.setObject(1, value)
            if (shouldIgnoreField) {
                statement.setObject(2, ignoreProps[1].trim())
            }
            statement.executeQuery().let { result ->
                result.next()
                result.getBoolean(1)
            }
        }
        return !exists.also { fails ->
            if (fails) {
                error = message?.let { it(attribute, value) } ?: "The $attribute is already used."
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
