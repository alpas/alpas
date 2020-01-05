package dev.alpas.auth

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.timestamp
import java.time.Instant

internal object PasswordResetTokens : Table("password_reset_tokens") {
    val email = varchar("email", 255).index()
    val token = varchar("token", 255)
    val createdAt = timestamp("created_at").nullable()
}

internal data class PasswordResetToken(val token: String, val createdAt: Instant?)
