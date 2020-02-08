package dev.alpas.auth

import dev.alpas.ozone.Ozone
import dev.alpas.ozone.OzoneTable
import dev.alpas.ozone.bigIncrements
import me.liuwj.ktorm.schema.Column
import me.liuwj.ktorm.schema.SqlType
import me.liuwj.ktorm.schema.varchar
import java.util.*

open class BaseUsersTable<E : BaseUser<E>> : OzoneTable<E>("users") {
    open val id by bigIncrements().bindTo { it.id }
    open val password by varchar("password").bindTo { it.password }
    open val email by varchar("email").bindTo { it.email }
    open val name by varchar("name").bindTo { it.name }
    open val createdAt by createdAt()
    open val updatedAt by updatedAt()

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
    companion object : Ozone.Of<User>()
}
