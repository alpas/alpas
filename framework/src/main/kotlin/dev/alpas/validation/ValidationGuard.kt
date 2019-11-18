package dev.alpas.validation

import dev.alpas.http.HttpCall
import dev.alpas.http.RequestError

open class ValidationGuard(val shouldFailFast: Boolean = false) {
    internal var inJsonBodyContext: Boolean = false
    private val rules = mutableListOf<Rule>()
    lateinit var call: HttpCall
        internal set

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

    private fun validate(attribute: String, errorBag: ErrorBag, rules: Iterable<Rule>) {
        rules.forEach {
            if (!it.check(attribute, call)) {
                errorBag.add(RequestError(attribute, call.params(attribute), it.error))
                if (shouldFailFast) {
                    return
                }
            }
        }
    }

    protected open fun rules(): Map<String, Iterable<Rule>> = mapOf()
    open fun handleError(errorBag: ErrorBag): Boolean {
        return false
    }
}
