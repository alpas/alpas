package dev.alpas.auth

import dev.alpas.auth.notifications.ResetPassword
import dev.alpas.exceptions.ValidationException
import dev.alpas.hashing.Hasher
import dev.alpas.http.HttpCall
import dev.alpas.http.RequestError
import dev.alpas.make
import dev.alpas.notifications.NotificationDispatcher
import dev.alpas.orAbort
import dev.alpas.secureRandomString
import dev.alpas.validation.Email
import dev.alpas.validation.ErrorBag
import dev.alpas.validation.Required
import dev.alpas.validation.Rule
import me.liuwj.ktorm.dsl.delete
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.dsl.insert

interface HandlesForgottenPassword {
    fun afterResetLinkSentRedirectTo(call: HttpCall): String? = null

    @Suppress("unused")
    fun showResetLinkRequestForm(call: HttpCall) {
        call.render("auth.passwords.reset_email")
    }

    @Suppress("unused")
    fun sendResetLinkEmail(call: HttpCall) {
        validate(call)
        val email = call.param("email").orAbort()
        val user = call.userProvider?.findByUsername(email)
        if (user == null) {
            throwValidationError(RequestError("email", email, message = "User with that email doesn't exist"))
            call.redirect().back()
        } else {
            val token = createToken(call, user)
            sendPasswordResetNotification(token, call, user)
            onResetLinkSentSuccess(call)
        }
    }

    fun onResetLinkSentSuccess(call: HttpCall) {
        call.session.flash("success", "We just emailed you a password reset link!")
        afterResetLinkSentRedirectTo(call)?.let {
            call.redirect().to(it)
        } ?: call.redirect().back()
    }

    fun throwValidationError(requestError: RequestError) {
        throw ValidationException(errorBag = ErrorBag(requestError))
    }

    private fun createToken(call: HttpCall, user: Authenticatable): String {
        val email = user.email.orAbort()
        // delete existing password forgot tokens for this user, if any
        PasswordResetTokens.delete { it.email eq email }
        val hasher = call.make<Hasher>()
        val rawToken = secureRandomString(40)
        val token = hasher.hash(rawToken)
        PasswordResetTokens.insert {
            it.email to email
            it.token to token
            it.createdAt to call.nowInCurrentTimezone().toInstant()
        }
        return rawToken
    }

    fun sendPasswordResetNotification(token: String, call: HttpCall, user: Authenticatable) {
        call.make<NotificationDispatcher>().dispatch(ResetPassword(token, call), user)
    }

    fun validate(call: HttpCall) {
        call.applyRules(validationRules())
    }

    fun validationRules(): Map<String, Iterable<Rule>> {
        return mapOf(
            "email" to listOf(Required(), Email())
        )
    }
}
