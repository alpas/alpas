package dev.alpas.auth

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.`java-time`.timestamp

internal object Users : LongIdTable("users") {
    val password = varchar("password", 255)
    val email = varchar("email", 255).uniqueIndex()
    val name = varchar("name", 255).nullable()
    val createdAt = timestamp("created_at").nullable()
    val updatedAt = timestamp("updated_at").nullable()
}

@Suppress("unused")
open class User(id: EntityID<Long>) : LongEntity(id), Authenticatable {
    companion object : LongEntityClass<User>(Users)

    override var password by Users.password
    override var email by Users.email
    open var name by Users.name
    open var createdAt by Users.createdAt
    open var updatedAt by Users.updatedAt

    override fun id() = id.value
}

internal object VerifiableUsers : LongIdTable("users") {
    val emailVerifiedAt = timestamp("email_verified_at").nullable()
}
