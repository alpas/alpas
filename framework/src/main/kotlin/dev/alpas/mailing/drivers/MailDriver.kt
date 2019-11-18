package dev.alpas.mailing.drivers

import dev.alpas.mailing.MailMessage

interface MailDriver {
    fun send(mail: MailMessage)
}
