package dev.alpas.auth

import dev.alpas.notifications.Notifiable

interface Authenticatable : Notifiable {
    val password: String
    val email: String?
    val mustVerifyEmail
        get() = true

    fun isEmailVerified(): Boolean {
        return false
    }

    fun primaryKey(): String {
        return "id"
    }

    fun id(): Long
}

interface UserProvider {
    fun findByUsername(username: Any): Authenticatable?
    fun findByPrimaryKey(id: Any): Authenticatable?
}
