package dev.alpas.auth

import dev.alpas.exceptions.ValidationException
import dev.alpas.http.HttpCall
import dev.alpas.http.RequestError
import dev.alpas.orAbort
import dev.alpas.validation.ErrorBag
import dev.alpas.validation.Required
import dev.alpas.validation.Rule

interface HandlesUserLogin {
    fun afterLoginRedirectTo(call: HttpCall) = "/home"
    fun afterLogoutRedirectTo(call: HttpCall) = "/"
    fun username() = "email"

    @Suppress("unused")
    fun showLoginForm(call: HttpCall) {
        call.render("auth.login")
    }

    @Suppress("unused")
    fun login(call: HttpCall) {
        validate(call)
        if (attemptLogin(call)) {
            onLoginSuccess(call)
        } else {
            onLoginFail(call)
        }
    }

    @Suppress("unused")
    fun logout(call: HttpCall) {
        call.authChannel.logout()
        call.redirect().to(afterLogoutRedirectTo(call))
    }

    fun onLoginFail(call: HttpCall) {
        val errorBag = ErrorBag(RequestError(username(), message = "Credentials don't match"))
        throw ValidationException(errorBag = errorBag)
    }

    fun onLoginSuccess(call: HttpCall) {
        call.session.regenerate()
        call.redirect().intended(default = afterLoginRedirectTo(call))
    }

    fun attemptLogin(call: HttpCall): Boolean {
        return call.authChannel.attempt(
            call.stringParam(username()),
            call.stringParam("password"),
            call.param("remember") != null
        )
    }

    fun validate(call: HttpCall) {
        call.applyRules(validationRules())
    }

    fun validationRules(): Map<String, Iterable<Rule>> {
        return mapOf(
            username() to listOf(Required()),
            "password" to listOf(Required())
        )
    }
}
