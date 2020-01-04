package dev.alpas.auth

import dev.alpas.auth.notifications.VerifyEmail
import dev.alpas.hashing.Hasher
import dev.alpas.http.HttpCall
import dev.alpas.ozone.validation.Unique
import dev.alpas.make
import dev.alpas.notifications.NotificationDispatcher
import dev.alpas.orAbort
import dev.alpas.validation.Confirm
import dev.alpas.validation.Email
import dev.alpas.validation.Max
import dev.alpas.validation.Min
import dev.alpas.validation.Required
import dev.alpas.validation.Rule
import me.liuwj.ktorm.dsl.insertAndGenerateKey

interface HandlesUserRegistration {
    fun afterRegisterRedirectTo(call: HttpCall) = "/login"
    fun username() = "email"

    @Suppress("unused")
    fun showRegistrationForm(call: HttpCall) {
        call.render("auth.register")
    }

    @Suppress("unused")
    fun register(call: HttpCall) {
        validate(call)
        val user = createUser(call)
        onRegistrationSuccess(call, user)
    }

    fun createUser(call: HttpCall): Authenticatable {
        val now = call.nowInCurrentTimezone().toInstant()
        val id = UsersTable.insertAndGenerateKey {
            it.name to call.param("name")
            it.password to call.make<Hasher>().hash(call.paramAsString("password").orAbort())
            it.email to call.param("email")
            it.createdAt to now
            it.updatedAt to now
        }
        return call.userProvider?.findByPrimaryKey(id)
            .orAbort("Couldn't find user with id $id. This shouldn't have happened!")
    }

    fun onRegistrationSuccess(call: HttpCall, user: Authenticatable) {
        if (user.mustVerifyEmail) {
            sendVerificationNotice(call, user)
        }
        call.redirect().to(afterRegisterRedirectTo(call))
    }

    fun sendVerificationNotice(call: HttpCall, user: Authenticatable) {
        call.make<NotificationDispatcher>().dispatch(VerifyEmail(call), user)
    }

    fun validate(call: HttpCall) {
        call.applyRules(validationRules(call))
    }

    fun validationRules(call: HttpCall): Map<String, Iterable<Rule>> {
        return mapOf(
            username() to listOf(Required(), Email(), Max(255), Unique("users", "email")),
            "name" to listOf(Required(), Max(64)),
            "password" to listOf(Required(), Confirm(), Min(8))
        )
    }
}
