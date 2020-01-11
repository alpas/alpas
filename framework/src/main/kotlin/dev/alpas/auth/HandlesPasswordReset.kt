package dev.alpas.auth

import dev.alpas.exceptions.ValidationException
import dev.alpas.hashing.Hasher
import dev.alpas.http.HttpCall
import dev.alpas.http.RequestError
import dev.alpas.make
import dev.alpas.orAbort
import dev.alpas.validation.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

interface HandlesPasswordReset {
    fun afterResetRedirectTo(call: HttpCall) = "/"

    @Suppress("unused")
    fun showResetForm(call: HttpCall) {
        call.render("auth.passwords.reset", call.onlyParams("email", "token"))
    }

    @Suppress("unused")
    fun reset(call: HttpCall) {
        transaction {
            validate(call)
            val user = call.make<UserProvider>().findByUsername(call.param("email").orAbort())
                ?: onResetFail(call, RequestError("email", message = "User doesn't exist"))
            val tokenIsValid = verifyToken(call, user)
            if (!tokenIsValid) {
                onResetFail(call, RequestError("token", message = "Invalid token"))
            }

            PasswordResetTokens.deleteWhere { PasswordResetTokens.email eq user.email.orAbort() }
            resetPassword(call, user)
            onResetSuccess(call)
        }
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

        val resetToken = PasswordResetTokens.select {
            PasswordResetTokens.email eq email
        }.map {
            // since we don't have a primary key id for this table, we'll spin our own "entity" class
            PasswordResetToken(it[PasswordResetTokens.token], it[PasswordResetTokens.createdAt])
        }.firstOrNull() ?: return false

        val tokenExpirationMinutes = call.make<AuthConfig>().passwordResetTokenExpiration.toMinutes()
        val hasExpired = resetToken.createdAt
            ?.plusSeconds(tokenExpirationMinutes * 60)
            ?.isBefore(call.nowInCurrentTimezone().toInstant())
            ?: true
        return !hasExpired && call.make<Hasher>().verify(call.paramAsString("token"), resetToken.token)
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
    val where = Users.id.eq(id())
    Users.update({ where }) {
        it[password] = newPassword
    }
}
