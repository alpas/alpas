package dev.alpas.auth

import dev.alpas.config
import dev.alpas.exceptions.ValidationException
import dev.alpas.hashing.Hasher
import dev.alpas.http.HttpCall
import dev.alpas.http.RequestError
import dev.alpas.lodestar.orAbort
import dev.alpas.make
import dev.alpas.validation.Confirm
import dev.alpas.validation.Email
import dev.alpas.validation.ErrorBag
import dev.alpas.validation.Min
import dev.alpas.validation.Required
import dev.alpas.validation.Rule
import me.liuwj.ktorm.dsl.delete
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.dsl.select
import me.liuwj.ktorm.dsl.update
import me.liuwj.ktorm.dsl.where

interface HandlesPasswordReset {
    fun afterResetRedirectTo(call: HttpCall) = "/"

    @Suppress("unused")
    fun showResetForm(call: HttpCall) {
        call.render("auth.passwords.reset", call.onlyParams("email", "token"))
    }

    @Suppress("unused")
    fun reset(call: HttpCall) {
        validate(call)
        val user = call.make<UserProvider>().findByUsername(call.param("email").orAbort())
            ?: onResetFail(call, RequestError("email", message = "User doesn't exist"))
        val tokenIsValid = verifyToken(call, user)
        if (!tokenIsValid) {
            onResetFail(call, RequestError("token", message = "Invalid token"))
        }

        TokensTable.delete { it.email eq user.email.orAbort() }
        resetPassword(call, user)
        onResetSuccess(call)
    }

    fun resetPassword(call: HttpCall, user: Authenticatable) {
        user.updatePassword(call.make<Hasher>().hash(call.paramAsString("password").orAbort()))
        call.authChannel.login(user)
    }

    fun onResetFail(call: HttpCall, error: RequestError): Nothing {
        throwValidationError(error)
    }

    fun onResetSuccess(call: HttpCall) {
        call.redirect().to(afterResetRedirectTo(call))
    }

    private fun verifyToken(call: HttpCall, user: Authenticatable): Boolean {
        val email = user.email.orAbort()
        val resetTokenQuery = TokensTable.select(
            TokensTable.email,
            TokensTable.token,
            TokensTable.createdAt
        ).where { TokensTable.email eq email }.firstOrNull() ?: return false

        val createdAt = resetTokenQuery[TokensTable.createdAt]
        val resetToken = resetTokenQuery[TokensTable.token]

        val tokenExpirationMinutes = call.config<AuthConfig>().passwordResetTokenExpiration.toMinutes()
        val hasExpired = createdAt
            ?.plusSeconds(tokenExpirationMinutes * 60)
            ?.isBefore(call.nowInCurrentTimezone().toInstant())
            ?: true
        return !hasExpired && call.make<Hasher>().verify(call.paramAsString("token"), resetToken)
    }

    fun throwValidationError(requestError: RequestError): Nothing {
        throw ValidationException(errorBag = ErrorBag(requestError))
    }

    fun validate(call: HttpCall) {
        call.applyRules(validationRules())
    }

    fun validationRules(): Map<String, Iterable<Rule>> {
        return mapOf(
            "token" to listOf(Required()),
            "email" to listOf(Required(), Email()),
            "password" to listOf(Required(), Confirm(), Min(8))
        )
    }
}

private fun Authenticatable.updatePassword(newPassword: String) {
    val id = this.id
    UsersTable.update {
        it.password to newPassword
        where { it.id eq id }
    }
}
