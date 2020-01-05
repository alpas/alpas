package dev.alpas.auth

import dev.alpas.notifications.Notifiable

interface Authenticatable : Notifiable {
    val password: String
    val email: String?
    val mustVerifyEmail
        get() = true

    fun id(): Long
    fun isEmailVerified() = false
}

interface UserProvider {
    fun findByUsername(username: Any): Authenticatable?
    fun findByPrimaryKey(id: Any): Authenticatable?
}
