package dev.alpas.auth

import dev.alpas.auth.notifications.VerifyEmail
import dev.alpas.hashing.Hasher
import dev.alpas.http.HttpCall
import dev.alpas.make
import dev.alpas.notifications.NotificationDispatcher
import dev.alpas.orAbort
import dev.alpas.ozone.validation.Unique
import dev.alpas.validation.*
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction

interface HandlesUserRegistration {
    fun afterRegisterRedirectTo(call: HttpCall) = "/login"
    fun username() = "email"

    @Suppress("unused")
    fun showRegistrationForm(call: HttpCall) {
        call.render("auth.register")
    }

    @Suppress("unused")
    fun register(call: HttpCall) {
        transaction {
            validate(call)
            val user = createUser(call)
            onRegistrationSuccess(call, user)
        }
    }

    fun createUser(call: HttpCall): Authenticatable {
        val now = call.nowInCurrentTimezone().toInstant()
        val id = Users.insertAndGetId {
            it[name] = call.paramAsString("name")
            it[password] = call.make<Hasher>().hash(call.paramAsString("password").orAbort())
            it[email] = call.paramAsString("email").orAbort()
            it[createdAt] = now
            it[updatedAt] = now
        }.value

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
