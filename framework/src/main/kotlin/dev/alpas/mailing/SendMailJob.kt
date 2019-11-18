package dev.alpas.mailing

import dev.alpas.Container
import dev.alpas.config
import dev.alpas.mailing.drivers.MailDriver
import dev.alpas.queue.Job

class SendMailJob(val mail: MailMessage) : Job() {
    override fun invoke(container: Container) {
        this(container.config<MailConfig>().driver())
    }

    operator fun invoke(mailDriver: MailDriver) {
        mailDriver.send(mail)
    }
}
