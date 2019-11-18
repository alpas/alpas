package dev.alpas.mailing

open class MailDriverConfig(
    val username: String? = null,
    val password: String? = null,
    val host: String? = null,
    val port: Int? = null,
    val defaultFromName: String? = null,
    val defaultFromAddress: String? = null
)
