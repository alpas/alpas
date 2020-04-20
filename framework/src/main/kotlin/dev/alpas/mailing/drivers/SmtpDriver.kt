package dev.alpas.mailing.drivers

import dev.alpas.Environment
import dev.alpas.mailing.MailDriverConfig
import dev.alpas.mailing.MailMessage
import org.simplejavamail.api.mailer.config.TransportStrategy
import org.simplejavamail.config.ConfigLoader
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.mailer.MailerBuilder
import java.time.Duration
import java.util.*

open class SmtpDriver(env: Environment, config: MailDriverConfig = MailDriverConfig()) : MailDriver {
    open val host = config.host ?: env("MAIL_HOST", "smtp.mailtrap.io")
    open val port = config.port ?: env("MAIL_PORT", 587)
    open val username = config.username ?: env("MAIL_USERNAME")
    open val password = config.password ?: env("MAIL_PASSWORD")
    open val proxyHost = env("MAIL_PROXY_HOST")
    open val proxyPort = env("MAIL_PROXY_PORT")
    open val proxyUsername = env("MAIL_PROXY_USERNAME")
    open val proxyPassword = env("MAIL_PROXY_PASSWORD")
    open val timeout: Duration = Duration.ofSeconds(env("MAIL_SESSSION_TIMEOUT", 10).toLong())
    open val defaultFromName = config.defaultFromName ?: env("MAIL_FROM_NAME", "Postmaster")
    open val defaultFromAddress = config.defaultFromAddress ?: env("MAIL_FROM_ADDRESS", "hello@example.com")

    private val mailer by lazy {
        val builder = MailerBuilder
            .withSMTPServer(host, port, username, password)
            .withTransportStrategy(TransportStrategy.SMTP_TLS)
            .withSessionTimeout(timeout.seconds.toInt())
            .clearEmailAddressCriteria() // turns off email validation
            .withDebugLogging(env.isDev)

        if (proxyHost != null) {
            builder.withProxy(proxyHost, proxyPort?.toInt(), proxyUsername, proxyPassword)
        }
        builder.buildMailer()
    }

    init {
        loadGlobalProperties()
    }

    override suspend fun send(mail: MailMessage) {
        val email = EmailBuilder.startingBlank()
            .to(mail.to)
            .withSubject(mail.subject)
            .withHTMLText(mail.message ?: "")
            .buildEmail()
        mailer.sendMail(email)
    }

    private fun loadGlobalProperties() {
        val properties = Properties()
        properties[ConfigLoader.Property.DEFAULT_FROM_ADDRESS.key()] = defaultFromAddress
        properties[ConfigLoader.Property.DEFAULT_FROM_NAME.key()] = defaultFromName
        ConfigLoader.loadProperties(properties, false)
    }
}
