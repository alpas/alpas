package dev.alpas.auth

import me.liuwj.ktorm.schema.Table
import me.liuwj.ktorm.schema.timestamp
import me.liuwj.ktorm.schema.varchar

internal typealias TokensTable = PasswordResetTokens

internal object PasswordResetTokens : Table<Nothing>("password_reset_tokens") {
    val email by varchar("email")
    val token by varchar("token")
    val createdAt by timestamp("created_at")
}
