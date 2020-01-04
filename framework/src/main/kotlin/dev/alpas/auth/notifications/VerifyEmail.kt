package dev.alpas.auth.notifications

import dev.alpas.Container
import dev.alpas.auth.AuthConfig
import dev.alpas.auth.Authenticatable
import dev.alpas.mailing.MailMessage
import dev.alpas.make
import dev.alpas.notifications.MailableNotification
import dev.alpas.notifications.channels.MailChannel
import dev.alpas.notifications.channels.NotificationChannel
import dev.alpas.orAbort
import dev.alpas.routing.UrlGenerator
import kotlin.reflect.KClass

class VerifyEmail(private val container: Container) : MailableNotification<Authenticatable> {
    override fun channels(notifiable: Authenticatable): List<KClass<out NotificationChannel>> {
        return listOf(MailChannel::class)
    }

    override fun toMail(notifiable: Authenticatable): MailMessage {
        val expiration = container.make<AuthConfig>().emailVerificationExpiration
        val verificationUrl =
            container.make<UrlGenerator>().signedRoute("verification.verify", mapOf("id" to notifiable.id), expiration)

        return MailMessage().apply {
            to = notifiable.email.orAbort()
            subject = "Verify Email"
            view(
                "auth.emails.verify",
                mapOf(
                    "verificationUrl" to verificationUrl?.toExternalForm(),
                    "username" to notifiable.properties["name"]
                )
            )
        }
    }
}
