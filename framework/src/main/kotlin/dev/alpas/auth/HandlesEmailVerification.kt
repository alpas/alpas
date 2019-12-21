package dev.alpas.auth

import dev.alpas.auth.notifications.VerifyEmail
import dev.alpas.http.HttpCall
import dev.alpas.make
import dev.alpas.notifications.NotificationDispatcher
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.dsl.update
import me.liuwj.ktorm.schema.InstantSqlType

interface HandlesEmailVerification {
    fun ifVerifiedRedirectTo(call: HttpCall) = "/home"

    @Suppress("unused")
    fun showVerificationRequiredNotice(call: HttpCall) {
        if (call.user.isEmailVerified()) {
            call.redirect().to(ifVerifiedRedirectTo(call))
        } else {
            call.render("auth.verify")
        }
    }

    @Suppress("unused")
    fun verify(call: HttpCall) {
        // The user is authenticated by this time
        val user = call.user
        if (call.param("id") != user.properties[user.primaryKey()].toString()) {
            throw AuthorizationException()
        }
        if (user.isEmailVerified()) {
            call.redirect().to(ifVerifiedRedirectTo(call))
        } else {
            user.verifyEmail(call)
            onVerificationSuccess(call)
        }
    }

    fun onVerificationSuccess(call: HttpCall) {
        call.redirect().to(ifVerifiedRedirectTo(call))
    }

    @Suppress("unused")
    fun resend(call: HttpCall) {
        val user = call.user
        if (user.isEmailVerified()) {
            call.redirect().to(ifVerifiedRedirectTo(call))
        } else {
            sendVerificationNotice(call, user)
            call.redirect().back()
        }
    }

    fun sendVerificationNotice(call: HttpCall, user: Authenticatable) {
        call.make<NotificationDispatcher>().dispatch(VerifyEmail(call), user)
    }
}

private fun Authenticatable.verifyEmail(call: HttpCall) {
    UsersTable.update {
        it.bind("email_verified_at", InstantSqlType) to call.nowInCurrentTimezone().toInstant()
        where { it.id eq id }
    }
}
