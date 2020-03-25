package dev.alpas.mailing

import dev.alpas.Container
import dev.alpas.config
import dev.alpas.mailing.drivers.MailDriver
import dev.alpas.queue.job.Job

class SendMailJob(val mail: MailMessage) : Job() {
    override suspend fun invoke(container: Container) {
        invoke(container.config<MailConfig>().driver())
    }

    suspend operator fun invoke(mailDriver: MailDriver) {
        mailDriver.send(mail)
    }
}
