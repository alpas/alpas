package dev.alpas.mailing

import dev.alpas.Container
import dev.alpas.config
import dev.alpas.mailing.drivers.MailDriver
import dev.alpas.queue.job.Job

@Suppress("MemberVisibilityCanBePrivate")
class SendMailJob(val mail: MailMessage, val driverName: String? = null) : Job() {
    override suspend fun invoke(container: Container) {
        invoke(container.config<MailConfig>().driver(driverName))
    }

    suspend operator fun invoke(mailDriver: MailDriver) {
        mailDriver.send(mail)
    }
}
