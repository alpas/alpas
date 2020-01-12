package dev.alpas.runner.configs

import dev.alpas.Environment
import dev.alpas.mailing.drivers.LocalMailDriver
import dev.alpas.mailing.drivers.SmtpDriver
import dev.alpas.mailing.MailConfig as BaseConfig

@Suppress("unused")
class MailConfig(env: Environment) : BaseConfig(env) {
    init {
        addDriver("smtp", lazy { SmtpDriver(env) })
        addDriver("local", lazy { LocalMailDriver(env) })
    }
}
