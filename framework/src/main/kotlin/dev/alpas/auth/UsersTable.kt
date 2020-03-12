package dev.alpas.auth

import dev.alpas.hashEquals
import dev.alpas.ozone.OzoneEntity
import dev.alpas.ozone.OzoneTable
import dev.alpas.ozone.bigIncrements
import dev.alpas.ozone.string
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.dsl.update
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
    open val emailVerifiedAt by timestamp("email_verified_at").nullable().bindTo { it.emailVerifiedAt }
    open val rememberToken by string("remember_token", 100).nullable().bindTo { it.rememberToken }
    open val createdAt by createdAt()
    open val updatedAt by updatedAt()

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

    override fun updateRememberToken(authenticatable: Authenticatable, token: String) {
        val count = update {
            rememberToken to token
            where {
                id eq authenticatable.id
            }
        }
        if (count > 0) {
            authenticatable.rememberToken = token
        }
    }

    override fun findByToken(rememberCookieToken: RememberCookieToken): Authenticatable? {
        val user = findByPrimaryKey(rememberCookieToken.id()) ?: return null

        return user.rememberToken?.let { token ->
            if (hashEquals(token, rememberCookieToken.token())) user else null
        }
    }
}

internal object UsersTable : BaseUsersTable<User>() {}

internal interface User : BaseUser<User> {
    companion object : OzoneEntity.Of<User>()
}
