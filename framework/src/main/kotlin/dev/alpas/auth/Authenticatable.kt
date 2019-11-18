package dev.alpas.auth

import dev.alpas.notifications.Notifiable

interface Authenticatable : Notifiable {
    val id: Long
    val password: String
    val email: String?
    // this will be set by ktorm
    val properties: Map<String, Any?>
    val mustVerifyEmail
        get() = true

    fun isEmailVerified(): Boolean {
        return properties["emailVerifiedAt"] != null
    }

    fun primaryKey(): String {
        return "id"
    }
}

interface UserProvider {
    fun findByUsername(username: Any): Authenticatable?
    fun findByPrimaryKey(id: Any): Authenticatable?
}
