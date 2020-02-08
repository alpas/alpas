@file:Suppress("UNCHECKED_CAST")

package dev.alpas.ozone

import dev.alpas.extensions.toSnakeCase
import me.liuwj.ktorm.dsl.and
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.entity.Entity
import me.liuwj.ktorm.entity.findList
import me.liuwj.ktorm.entity.findOne
import me.liuwj.ktorm.schema.BaseTable
import me.liuwj.ktorm.schema.Column
import me.liuwj.ktorm.schema.ColumnDeclaring
import org.atteo.evo.inflector.English

inline fun <reified E : Entity<E>, reified T : BaseTable<E>> Entity<*>.hasManyBy(
    table: T,
    cacheKey: String? = null,
    predicate: (T) -> ColumnDeclaring<Boolean>
): List<E> {
    val name = cacheKey ?: table.hasManyCacheKey
    return this[name] as? List<E> ?: table.findList { predicate(table) }.also { this[name] = it }
}

inline fun <reified E : Entity<E>, reified T : BaseTable<E>> Entity<*>.hasMany(
    table: T,
    foreignKey: String? = null,
    localKey: String? = null,
    cacheKey: String? = null,
    predicate: (T) -> ColumnDeclaring<Boolean>
): List<E> {
    val name = cacheKey ?: table.hasManyCacheKey
    return this[name] as? List<E> ?: table.findList {
        val actualForeignKey = foreignKey ?: this.defaultForeignKey
        val foreignKeyValue = it[actualForeignKey] as Column<Any>
        (foreignKeyValue eq this[localKey ?: "id"]!!) and predicate(table)
    }.also { this[name] = it }
}

inline fun <reified E : Entity<E>, reified T : BaseTable<E>> Entity<*>.hasMany(
    table: T,
    foreignKey: String? = null,
    localKey: String? = null,
    cacheKey: String? = null
): List<E> {
    val name = cacheKey ?: table.hasManyCacheKey
    return this[name] as? List<E> ?: table.findList {
        val actualForeignKey = foreignKey ?: this.defaultForeignKey
        val foreignKeyValue = it[actualForeignKey] as Column<Any>
        foreignKeyValue eq this[localKey ?: "id"]!!
    }.also { this[name] = it }
}


inline fun <reified E : Entity<E>, reified T : BaseTable<E>> Entity<*>.hasOneBy(
    table: T,
    cacheKey: String? = null,
    predicate: (T) -> ColumnDeclaring<Boolean>
): E? {
    val name = cacheKey ?: table.hasOneCacheKey
    return this[name] as? E ?: table.findOne(predicate).also { this[name] = it }
}

inline fun <reified E : Entity<E>, reified T : BaseTable<E>> Entity<*>.hasOne(
    table: T,
    foreignKey: String? = null,
    localKey: String? = null,
    cacheKey: String? = null,
    predicate: (T) -> ColumnDeclaring<Boolean>
): E? {
    val name = cacheKey ?: table.hasOneCacheKey
    return this[name] as? E ?: table.findOne {
        val actualForeignKey = foreignKey ?: this.defaultForeignKey
        val foreignKeyValue = it[actualForeignKey] as Column<Any>
        (foreignKeyValue eq this[localKey ?: "id"]!!) and predicate(table)
    }.also { this[name] = it }
}

inline fun <reified E : Entity<E>, reified T : BaseTable<E>> Entity<*>.hasOne(
    table: T,
    foreignKey: String? = null,
    localKey: String? = null,
    cacheKey: String? = null
): E? {
    val name = cacheKey ?: table.hasOneCacheKey
    return this[name] as? E ?: table.findOne {
        val actualForeignKey = foreignKey ?: this.defaultForeignKey
        val foreignKeyValue = it[actualForeignKey] as Column<Any>
        foreignKeyValue eq this[localKey ?: "id"]!!
    }.also { this[name] = it }
}

val BaseTable<*>.hasOneCacheKey: String
    get() = "${English.plural(tableName, 1)}_cache_key"

val BaseTable<*>.hasManyCacheKey: String
    get() = "${tableName}_cache_key"

val Entity<*>.defaultForeignKey: String
    get() = "${this.entityClass.simpleName?.toSnakeCase()!!}_id"
