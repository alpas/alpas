package dev.alpas.auth

import dev.alpas.config
import dev.alpas.exceptions.ValidationException
import dev.alpas.hashing.Hasher
import dev.alpas.http.HttpCall
import dev.alpas.http.RequestError
import dev.alpas.make
import dev.alpas.orAbort
import dev.alpas.validation.*
import me.liuwj.ktorm.dsl.*

interface HandlesPasswordReset {
    fun afterResetRedirectTo(call: HttpCall) = "/"

    @Suppress("unused")
    fun showResetForm(call: HttpCall) {
        call.render("auth.passwords.reset", call.params("email", "token"))
    }

    @Suppress("unused")
    fun reset(call: HttpCall) {
        validate(call)
        val user = call.userProvider?.findByUsername(call.param("email").orAbort())
            ?: onResetFail(call, RequestError("email", message = "User doesn't exist"))
        val tokenIsValid = verifyToken(call, user)
        if (!tokenIsValid) {
            onResetFail(call, RequestError("token", message = "Invalid token"))
        }

        PasswordResetTokens.delete { it.email eq user.email.orAbort() }
        resetPassword(call, user)
        onResetSuccess(call)
    }

    fun resetPassword(call: HttpCall, user: Authenticatable) {
        user.updatePassword(call.make<Hasher>().hash(call.stringParam("password").orAbort()))
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
        val resetTokenQuery = PasswordResetTokens.select(
            PasswordResetTokens.email,
            PasswordResetTokens.token,
            PasswordResetTokens.createdAt
        ).where { PasswordResetTokens.email eq email }.firstOrNull() ?: return false

        val createdAt = resetTokenQuery[PasswordResetTokens.createdAt]
        val resetToken = resetTokenQuery[PasswordResetTokens.token]

        val tokenExpirationMinutes = call.config<AuthConfig>().passwordResetTokenExpiration.toMinutes()
        val hasExpired = createdAt
            ?.plusSeconds(tokenExpirationMinutes * 60)
            ?.isBefore(call.nowInCurrentTimezone().toInstant())
            ?: true
        return !hasExpired && call.make<Hasher>().verify(call.stringParam("token"), resetToken)
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
