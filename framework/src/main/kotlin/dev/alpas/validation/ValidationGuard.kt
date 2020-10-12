package dev.alpas.validation

import dev.alpas.auth.Authenticatable
import dev.alpas.http.HttpCall
import dev.alpas.http.RequestError
import dev.alpas.orAbort
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
        checkAuthentication()
        validate(attribute, errorBag, rules)
    }

    fun validate(errorBag: ErrorBag) {
        validate(rules(), errorBag)
    }

    fun validate(rules: Map<String, Iterable<Rule>>, errorBag: ErrorBag) {
        checkAuthentication()
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

    private fun checkAuthentication() {
        val shouldAllow = user()?.let { allow(it) } ?: allow()

        if (!shouldAllow) {
            handleUnauthorizedAccess()
        }
    }

    protected open fun handleUnauthorizedAccess() {
        call.abort(HttpStatus.UNAUTHORIZED_401)
    }

    private fun validate(attribute: String, errorBag: ErrorBag, rules: Iterable<Rule>) {
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

    fun string(key: String, message: String? = null, statusCode: Int = HttpStatus.NOT_FOUND_404): String {
        return validatedParams[key]?.toString().orAbort(message, statusCode)
    }

    fun long(key: String, message: String? = null, statusCode: Int = HttpStatus.NOT_FOUND_404): Long {
        return validatedParams[key]?.toString()?.toLongOrNull().orAbort(message, statusCode)
    }

    fun user(): Authenticatable? {
        return if (call.isAuthenticated) call.user else null
    }
}
