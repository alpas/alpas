package dev.alpas.auth

import dev.alpas.ozone.MigratingTable
import me.liuwj.ktorm.schema.timestamp
import me.liuwj.ktorm.schema.varchar

object PasswordResetTokens : MigratingTable<Nothing>("password_reset_tokens") {
    val email by varchar("email").index()
    val token by varchar("token")
    val createdAt by timestamp("created_at").nullable()
}
