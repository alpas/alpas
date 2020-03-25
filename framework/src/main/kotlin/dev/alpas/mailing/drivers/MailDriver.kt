package dev.alpas.mailing.drivers

import dev.alpas.mailing.MailMessage

interface MailDriver {
    suspend fun send(mail: MailMessage)
}
