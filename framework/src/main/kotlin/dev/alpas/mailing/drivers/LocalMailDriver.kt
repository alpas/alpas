package dev.alpas.mailing.drivers

import dev.alpas.Environment
import dev.alpas.mailing.MailMessage
import mu.KotlinLogging
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalMailDriver(env: Environment) : MailDriver {
    private val outputPath by lazy { env.storagePath("mails") }
    private val logger by lazy { KotlinLogging.logger {} }
    private val fromName = env("MAIL_FROM_NAME", "Postmaster")
    private val fromAddress = env("MAIL_FROM_ADDRESS", "hello@example.com")

    override suspend fun send(mail: MailMessage) {
        val datePrefix = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("y_MM_dd_Hmmss"))

        File(outputPath, "${datePrefix}_${mail.to}.html").apply {
            parentFile.mkdirs()
            bufferedWriter().use {
                appendText(
                    """
                   <!-- 
                   From: $fromName <$fromAddress>
                   To: ${mail.to}
                   Subject: ${mail.subject}
                   --> 
                """.trimIndent()
                )
                appendText(mail.message)
            }
            logger.info("Email for ${mail.to} written to: $path")
        }
    }
}
