package dev.alpas.auth

import dev.alpas.ozone.OzoneEntity
import dev.alpas.ozone.OzoneTable
import dev.alpas.ozone.bigIncrements
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.entity.findById
import me.liuwj.ktorm.entity.findOne
import me.liuwj.ktorm.schema.Column
import me.liuwj.ktorm.schema.SqlType
import me.liuwj.ktorm.schema.timestamp
import me.liuwj.ktorm.schema.varchar
import java.util.*

open class BaseUsersTable<E : BaseUser<E>> : OzoneTable<E>("users"), UserProvider {
    open val id by bigIncrements()
    open val password by varchar("password").bindTo { it.password }
    open val email by varchar("email").bindTo { it.email }
    open val name by varchar("name").bindTo { it.name }
    open val createdAt by createdAt()
    open val updatedAt by updatedAt()
    open val emailVerifiedAt by timestamp("email_verified_at").nullable().bindTo { it.emailVerifiedAt }

    fun <T : Any> bind(name: String, ofType: SqlType<T>): Column<*> {
        return try {
            this[name]
        } catch (e: NoSuchElementException) {
            this.registerColumn(name, ofType).getColumn()
        }
    }

    override fun findByUsername(username: Any): E? {
        return findOne { it.email.eq(username.toString()) }
    }

    override fun findByPrimaryKey(id: Any): E? {
        return findById(id)
    }
}

internal object UsersTable : BaseUsersTable<User>() {}

internal interface User : BaseUser<User> {
    companion object : OzoneEntity.Of<User>()
}
