package dev.alpas.notifications.channels

import dev.alpas.mailing.MailConfig
import dev.alpas.mailing.SendMailJob
import dev.alpas.notifications.MailableNotification
import dev.alpas.notifications.Notifiable
import dev.alpas.notifications.Notification
import dev.alpas.queue.QueueDispatcher
import kotlinx.coroutines.runBlocking

open class MailChannel(private val queueDispatcher: QueueDispatcher, private val mailConfig: MailConfig) : NotificationChannel {
    override fun <T : Notifiable> send(notification: Notification<T>, notifiable: T) {
        val mailableNotification = notification as MailableNotification<T>
        val message = mailableNotification.toMail(notifiable).apply { render(mailConfig.renderer()) }
        val job = SendMailJob(message, mailableNotification.mailDriverName(notifiable))
        if (notification.shouldQueue(notifiable)) {
            queueDispatcher.dispatch(job, notification.onQueue(notifiable), notification.queueConnection(notifiable))
        } else {
            runBlocking {
                job(mailConfig.driver(mailableNotification.mailDriverName(notifiable)))
            }
        }
    }
}
