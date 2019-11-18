package dev.alpas.validation

import dev.alpas.http.HttpCall
import org.hazlewood.connor.bottema.emailaddress.EmailAddressValidator

typealias ErrorMessage = ((String, Any?) -> String)?

class Max(private val length: Int, private val message: ErrorMessage = null) : Rule() {
    override fun check(attribute: String, value: Any?): Boolean {
        return ((value ?: "").toString().length < length).also {
            if (!it) {
                error =
                    message?.let { it(attribute, value) } ?: "The $attribute must be at most $length characters long."
            }
        }
    }
}

class Min(private val length: Int, private val message: ErrorMessage = null) : Rule() {
    override fun check(attribute: String, value: Any?): Boolean {
        return ((value ?: "").toString().length >= length).also {
            if (!it) {
                error =
                    message?.let { it(attribute, value) } ?: "The $attribute must be at least $length characters long."
            }
        }
    }
}

// Attribute must be present and the value must not be null or empty.
class Required(private val message: ErrorMessage = null) : Rule() {
    override fun check(attribute: String, value: Any?): Boolean {
        return (!value?.toString().isNullOrBlank()).also {
            if (!it) {
                error = message?.let { it(attribute, value) } ?: "The required field '$attribute' is missing, null, or empty."
            }
        }
    }
}

// Attribute must be present and the value must not be null. It can be empty.
class NotNull(private val message: ErrorMessage = null) : Rule() {
    override fun check(attribute: String, value: Any?): Boolean {
        return (value != null).also{
            if (!it) {
                error = message?.let { it(attribute, value) } ?: "The non null field '$attribute' is null."
            }
        }
    }
}

class MustBeInteger(private val message: ErrorMessage = null) : Rule() {
    override fun check(attribute: String, value: Any?): Boolean {
        val isValid = value?.toString()?.toLongOrNull() != null
        return isValid.also {
            if (!it) {
                error = message?.let { it(attribute, value) } ?: "The field '$attribute' must be an integer."
            }
        }
    }
}

class MustBeString(private val message: ErrorMessage = null) : Rule() {
    override fun check(attribute: String, value: Any?): Boolean {
        val isValid = value is String
        return isValid.also {
            if (!it) {
                error = message?.let { it(attribute, value) } ?: "The field '$attribute' must be a string."
            }
        }
    }
}

class MatchesRegularExpression(private val expression: String, private val message: ErrorMessage = null) : Rule() {
    override fun check(attribute: String, value: Any?): Boolean {
        return (value?.toString()?.matches(expression.toRegex()) == true).also {
            if (!it) {
                error = message?.let { it(attribute, value) } ?: "The field '$attribute' did not match the required format."
            }
        }
    }
}

class Confirm(private val message: ErrorMessage = null) : Rule() {
    override fun check(attribute: String, call: HttpCall): Boolean {
        val confirmAttribute1 = "${attribute}_confirm"
        val confirmAttribute2 = "confirm_$attribute"
        val value = call.param(attribute)
        val valueConfirm = call.param(confirmAttribute1) ?: call.param(confirmAttribute2)
        return (!valueConfirm?.toString().isNullOrBlank() && value == valueConfirm).also {
            if (!it) {
                error = message?.let { it(attribute, valueConfirm) } ?: "The $attribute confirmation does not match."
            }
        }
    }
}

class Email(private val message: ErrorMessage = null) : Rule() {
    override fun check(attribute: String, value: Any?): Boolean {
        return (!value?.toString().isNullOrBlank() && EmailAddressValidator.isValid(value?.toString())).also {
            if (!it) {
                error = message?.let { it(attribute, value) } ?: "$attribute is not a valid email address."
            }
        }
    }
}

class JsonField(private val rules: List<Rule>) : Rule() {
    override fun check(attribute: String, call: HttpCall): Boolean {
        if (!call.isJson || call.jsonBody == null) {
            return false.also {
                error = "The body does not have a valid json format"
            }
        }
        val value = call.jsonBody[attribute]
        rules.forEach { rule ->
            if (!rule.check(attribute, value)) {
                return false.also {
                    error = rule.error
                }
            }
        }
        return true
    }
}

fun ValidationGuard.max(length: Int, message: ErrorMessage = null): Rule {
    return rule(Max(length, message))
}

fun ValidationGuard.min(length: Int, message: ErrorMessage = null): Rule {
    return rule(Min(length, message))
}

fun ValidationGuard.required(message: ErrorMessage = null): Rule {
    return rule(Required(message))
}

fun ValidationGuard.notNull(message: ErrorMessage = null): Rule {
    return rule(NotNull(message))
}

fun ValidationGuard.confirm(message: ErrorMessage = null): Rule {
    return rule(Confirm(message))
}

fun ValidationGuard.email(message: ErrorMessage = null): Rule {
    return rule(Email(message))
}

fun ValidationGuard.mustBeInteger(message: ErrorMessage = null): Rule {
    return rule(MustBeInteger(message))
}

fun ValidationGuard.mustBeString(message: ErrorMessage = null): Rule {
    return rule(MustBeString(message))
}

fun ValidationGuard.regEx(expression: String, message: ErrorMessage = null): Rule {
    return rule(MatchesRegularExpression(expression, message))
}

fun ValidationGuard.inJsonBody(rules: ValidationGuard.() -> Rule) {
    inJsonBodyContext = true
    rules()
    inJsonBodyContext = false
}
