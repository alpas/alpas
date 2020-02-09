package dev.alpas.auth

import dev.alpas.md5
import dev.alpas.ozone.Ozone
import java.time.Instant

interface BaseUser<E : BaseUser<E>> : Ozone<E>, Authenticatable {
    override var id: Long
    override var password: String
    override var email: String?
    var name: String?
    var createdAt: Instant?
    var updatedAt: Instant?
    val emailVerifiedAt: Instant?

    @ExperimentalUnsignedTypes
    fun gravatarUrl(): String {
        val hash = email?.trim()?.toLowerCase()?.md5() ?: ""
        return "//www.gravatar.com/avatar/$hash?s=160&d=robohash"
    }

    override fun isEmailVerified() = emailVerifiedAt != null
}
