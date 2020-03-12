package dev.alpas.auth

import dev.alpas.notifications.Notifiable

interface Authenticatable : Notifiable {
    val id: Long
    val password: String
    val email: String?
    var rememberToken: String?
    val properties: Map<String, Any?>
    val mustVerifyEmail get() = true

    fun isEmailVerified() = properties.containsKey("emailVerifiedAt")
    fun primaryKey() = "id"
}

interface UserProvider {
    fun findByUsername(username: Any): Authenticatable?
    fun findByPrimaryKey(id: Any): Authenticatable?
    fun updateRememberToken(authenticatable: Authenticatable, token: String)
    fun findByToken(rememberCookieToken: RememberCookieToken): Authenticatable?
}
