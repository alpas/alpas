package dev.alpas.notifications.slack

import dev.alpas.notifications.Notifiable
import dev.alpas.notifications.Notification
import dev.alpas.notifications.channels.NotificationChannel
import dev.alpas.queue.QueueDispatcher
import kotlinx.coroutines.runBlocking

open class SlackChannel(private val queueDispatcher: QueueDispatcher) :
    NotificationChannel {
    override fun <T : Notifiable> send(notification: Notification<T>, notifiable: T) {
        val slackableNotification = notification as SlackableNotification<T>
        val message = slackableNotification.toSlack(notifiable)
        val job = SendSlackMessageJob(
            message,
            slackableNotification.webhook(message, notifiable)
        )
        if (notification.shouldQueue(notifiable)) {
            queueDispatcher.dispatch(job, notification.onQueue(notifiable), notification.queueConnection(notifiable))
        } else {
            runBlocking {
                job()
            }
        }
    }
}
