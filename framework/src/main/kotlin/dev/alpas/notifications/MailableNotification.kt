package dev.alpas.notifications

import dev.alpas.mailing.MailMessage

interface MailableNotification<T> {
    fun toMail(notifiable: T): MailMessage
}
