package dev.alpas.validation

import dev.alpas.auth.Authenticatable
import dev.alpas.http.HttpCall
import dev.alpas.http.RequestError
import org.eclipse.jetty.http.HttpStatus

open class ValidationGuard(val shouldFailFast: Boolean = false, inJsonBody: Boolean = false) {
    internal var inJsonBodyContext: Boolean = inJsonBody
    private val validatedParams = mutableMapOf<String, Any>()
    private val rules = mutableListOf<Rule>()
    lateinit var call: HttpCall
        internal set

    protected open fun allow(user: Authenticatable): Boolean {
        return true
    }

    protected open fun allow(): Boolean {
        return true
    }

    fun rule(rule: Rule): Rule {
        rules.add(rule)
        if (inJsonBodyContext) {
            rule.inJsonBody()
        }
        return rule
    }

    fun validate(attribute: String, errorBag: ErrorBag) {
        validate(attribute, errorBag, rules)
    }

    fun validate(errorBag: ErrorBag) {
        validate(rules(), errorBag)
    }

    fun validate(rules: Map<String, Iterable<Rule>>, errorBag: ErrorBag) {
        rules.forEach { (attribute, rules) ->
            validate(attribute, errorBag, rules)
            if (!errorBag.isEmpty() && shouldFailFast) {
                return
            }
        }
    }

    fun validatedParams(): Map<String, Any> {
        return validatedParams.toMap()
    }

    fun validatedParams(vararg keys: String): Map<String, Any> {
        return validatedParams.filterKeys {
            it in keys
        }
    }

    fun validatedParam(key: String): Any? {
        return validatedParams[key]
    }

    private fun validate(attribute: String, errorBag: ErrorBag, rules: Iterable<Rule>) {
        val shouldAllow = user()?.let { allow(it) } ?: allow()

        if (!shouldAllow) {
            call.abort(HttpStatus.UNAUTHORIZED_401)
        }
        rules.forEach {
            if (inJsonBodyContext || call.validateUsingJsonBody.get()) {
                it.inJsonBody()
            }
            val params = call.paramList(attribute) ?: emptyList()
            if (it.check(attribute, call)) {
                validatedParams[attribute] = if (params.count() == 1) params.first() else params
            } else {
                errorBag.add(RequestError(attribute, params, it.errorMessage(attribute)))
                if (shouldFailFast) {
                    return
                }
            }
        }
    }

    protected open fun rules(): Map<String, Iterable<Rule>> = emptyMap()

    open fun handleError(errorBag: ErrorBag) = false

    open fun afterSuccessfulValidation() {}

    fun params(vararg keys: String, firstValueOnly: Boolean = true): Map<String, Any?> {
        return call.params(*keys, firstValueOnly = firstValueOnly)
    }

    fun stringParam(key: String, message: String? = null, statusCode: Int = HttpStatus.NOT_FOUND_404): String {
        return call.stringParam(key, message, statusCode)
    }

    fun longParam(key: String, message: String? = null, statusCode: Int = HttpStatus.NOT_FOUND_404): Long {
        return call.longParam(key, message, statusCode)
    }

    fun user(): Authenticatable? {
        return if (call.isAuthenticated) call.user else null
    }
}
