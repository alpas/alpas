package dev.alpas.auth

import me.liuwj.ktorm.entity.Entity
import java.time.LocalDateTime

interface BaseUser<E : BaseUser<E>> : Entity<E>, Authenticatable {
    override var id: Long
    override var password: String
    override var email: String?
    var name: String?
    var createdAt: LocalDateTime?
    var updatedAt: LocalDateTime?
}
