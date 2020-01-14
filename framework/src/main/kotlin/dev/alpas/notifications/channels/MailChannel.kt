package dev.alpas.notifications.channels

import dev.alpas.mailing.MailConfig
import dev.alpas.mailing.SendMailJob
import dev.alpas.notifications.MailableNotification
import dev.alpas.notifications.Notifiable
import dev.alpas.notifications.Notification
import dev.alpas.queue.Queue
import dev.alpas.queue.QueueDispatcher
import dev.alpas.view.ViewRenderer

class MailChannel(
    private val queueDispatcher: QueueDispatcher, private val mailConfig: MailConfig, private val viewRenderer: ViewRenderer
) : NotificationChannel {

    override fun <T : Notifiable> send(notification: Notification<T>, notifiable: T) {
        val not = notification as MailableNotification<T>
        val message = not.toMail(notifiable).apply { render(viewRenderer) }
        val job = SendMailJob(message)
        if (notification.shouldQueue(notifiable)) {
            queueDispatcher.dispatch(job)
        } else {
            job(mailer)
        }
    }

    private val mailer by lazy { mailConfig.driver() }
}
