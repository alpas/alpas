package dev.alpas.auth

import me.liuwj.ktorm.entity.Entity
import me.liuwj.ktorm.schema.*
import java.util.NoSuchElementException

open class BaseUsersTable<E : BaseUser<E>> : Table<E>("users") {
    open val id by long("id").primaryKey().bindTo { it.id }
    open val password by varchar("password").bindTo { it.password }
    open val email by varchar("email").bindTo { it.email }
    open val name by varchar("name").bindTo { it.name }
    open val createdAt by timestamp("created_at").bindTo { it.createdAt }
    open val updatedAt by timestamp("updated_at").bindTo { it.updatedAt }

    fun <T : Any> bind(name: String, ofType: SqlType<T>): Column<*> {
        return try {
            this[name]
        } catch (e: NoSuchElementException) {
            this.registerColumn(name, ofType).getColumn()
        }
    }
}

internal object UsersTable : BaseUsersTable<User>() {}

internal interface User : BaseUser<User> {
    companion object : Entity.Factory<User>()
}
