package dev.alpas.notifications.console.stubs

internal class Stubs {
    companion object {
        fun notificationStub(): String {
            return """
                import dev.alpas.auth.Authenticatable
                import dev.alpas.mailing.MailMessage
                import dev.alpas.notifications.MailableNotification
                import dev.alpas.notifications.channels.MailChannel
                import dev.alpas.notifications.channels.NotificationChannel
                import kotlin.reflect.KClass

                class StubClazzName() : MailableNotification<Authenticatable> {
                    override fun channels(notifiable: Authenticatable): List<KClass<out NotificationChannel>> {
                        return listOf(MailChannel::class)
                    }

                    override fun toMail(notifiable: Authenticatable): MailMessage {
                        TODO("Return a mail message")
                    }
                }
            """.trimIndent()
        }
    }
}
