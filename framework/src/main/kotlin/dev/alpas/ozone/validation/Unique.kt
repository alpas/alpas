package dev.alpas.ozone.validation

import dev.alpas.http.HttpCall
import dev.alpas.validation.ErrorMessage
import dev.alpas.validation.Rule
import me.liuwj.ktorm.database.useConnection

class Unique(
    private val table: String,
    private val column: String,
    private val ignore: String? = null,
    private val message: ErrorMessage = null
) : Rule() {
    override fun check(attribute: String, call: HttpCall): Boolean {
        val value = call.param(attribute)
        val exists = useConnection {
            var query = "SELECT COUNT(1) FROM `$table` WHERE `$column` = ?"
            val ignoreProps = ignore?.split(":") ?: emptyList()
            val shouldIgnoreField = ignoreProps.count() == 2
            if (shouldIgnoreField) {
                query = "$query AND `${ignoreProps[0]}` != ?"
            }
            val statement = it.prepareStatement(query)
            statement.setObject(1, value)
            if (shouldIgnoreField) {
                statement.setObject(2, ignoreProps[1])
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
