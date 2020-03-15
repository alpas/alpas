package dev.alpas.validation

import dev.alpas.http.HttpCall

abstract class Rule {
    lateinit var error: String
        protected set

    protected var checkInJson = false
        private set

    open fun check(attribute: String, values: List<Any>?): Boolean {
        return check(attribute, values?.firstOrNull())
    }

    open fun check(attribute: String, value: Any?): Boolean {
        return false
    }

    open fun check(attribute: String, call: HttpCall): Boolean {
        return if (checkInJson) {
            check(attribute, call.jsonBody?.get(attribute))
        } else {
            check(attribute, call.paramList(attribute))
        }
    }

    fun inJsonBody(): Rule {
        checkInJson = true
        return this
    }

    fun errorMessage(attribute: String): String {
        return if (::error.isInitialized) {
            error
        } else {
            "$attribute has an error"
        }
    }
}
