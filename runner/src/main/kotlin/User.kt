package dev.alpas.runner

import dev.alpas.auth.Authenticatable
import dev.alpas.auth.UserProvider
import dev.alpas.md5
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.`java-time`.timestamp
import org.jetbrains.exposed.sql.transactions.transaction

object Users : LongIdTable("users"), UserProvider {
    val email = varchar("email", 255).uniqueIndex()
    val password = varchar("password", 255)
    val name = varchar("name", 255).nullable()
    val emailVerifiedAt = timestamp("email_verified_at").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override fun findByPrimaryKey(id: Any): Authenticatable? {
        return transaction {
            User.findById(id.toString().toLong())
        }
    }

    override fun findByUsername(username: Any): Authenticatable? {
        return transaction {
            User.find {
                email eq username.toString()
            }.firstOrNull()
        }
    }
}

class User(id: EntityID<Long>) : LongEntity(id), Authenticatable {
    companion object : LongEntityClass<User>(Users)

    override var email by Users.email
    override var password by Users.password
    var name by Users.name
    var emailVerifiedAt by Users.emailVerifiedAt
    var createdAt by Users.createdAt
    var updatedAt by Users.updatedAt

    @ExperimentalUnsignedTypes
    fun gravatarUrl(): String {
        val hash = email.trim().toLowerCase().md5()
        return "https://www.gravatar.com/avatar/$hash?s=160&d=robohash"
    }


    override fun id() = id.value

    override fun isEmailVerified() = emailVerifiedAt != null
}
