package dev.alpas.auth.notifications

import dev.alpas.Container
import dev.alpas.auth.AuthConfig
import dev.alpas.auth.Authenticatable
import dev.alpas.config
import dev.alpas.mailing.MailMessage
import dev.alpas.make
import dev.alpas.notifications.MailableNotification
import dev.alpas.notifications.channels.MailChannel
import dev.alpas.notifications.channels.NotificationChannel
import dev.alpas.orAbort
import dev.alpas.routing.UrlGenerator
import kotlin.reflect.KClass

class ResetPassword(private val token: String, private val container: Container) :
    MailableNotification<Authenticatable> {
    override fun channels(notifiable: Authenticatable): List<KClass<out NotificationChannel>> {
        return listOf(MailChannel::class)
    }

    override fun toMail(notifiable: Authenticatable): MailMessage {
        val tokenExpirationDuration = container.config<AuthConfig>().passwordResetTokenExpiration.toHours()
        val resetUrl = container.make<UrlGenerator>().route("password.reset", mapOf("token" to token))

        return MailMessage().apply {
            to = notifiable.email.orAbort()
            subject = "Reset Password"
            view(
                "auth.emails.reset", mapOf("resetUrl" to resetUrl, "tokenExpirationDuration" to tokenExpirationDuration)
            )
        }
    }
}
