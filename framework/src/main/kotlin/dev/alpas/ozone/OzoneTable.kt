package dev.alpas.ozone

import dev.alpas.extensions.toCamelCase
import me.liuwj.ktorm.schema.*
import java.time.Instant
import java.time.temporal.Temporal
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

abstract class OzoneTable<E : Ozone<E>>(tableName: String, alias: String? = null, entityClass: KClass<E>? = null) :
    Table<E>(tableName, alias, entityClass) {

    internal val metadataMap = hashMapOf<String, ColumnMetadata>()

    inline fun <C : Any, R : Ozone<R>> ColumnRegistration<C>.belongsTo(
        referenceTable: Table<R>,
        selector: (E) -> R?
    ) = apply { references(referenceTable, selector) }

    fun ColumnRegistration<String>.size(size: Int): ColumnRegistration<String> {
        val columnName = getColumn().name
        val metadata = metadataMap[columnName]
        if (metadata == null) {
            metadataMap[columnName] = ColumnMetadata(size = size)
        } else {
            metadataMap[columnName] = metadata.copy(size = size)
        }

        return this
    }

    fun <T : Any> ColumnRegistration<T>.default(default: T): ColumnRegistration<T> {
        val columnName = getColumn().name
        val metadata = metadataMap[columnName]
        if (metadata == null) {
            metadataMap[columnName] = ColumnMetadata(defaultValue = default)
        } else {
            metadataMap[columnName] = metadata.copy(defaultValue = default)
        }

        return this
    }

    fun <T : Temporal> ColumnRegistration<T>.useCurrent(): ColumnRegistration<T> {
        val columnName = getColumn().name
        val metadata = metadataMap[columnName]
        if (metadata == null) {
            metadataMap[columnName] = ColumnMetadata(useCurrentTimestamp = true)
        } else {
            metadataMap[columnName] = metadata.copy(useCurrentTimestamp = true)
        }

        return this
    }

    fun <T : Number> ColumnRegistration<T>.unsigned(): ColumnRegistration<T> {
        val columnName = getColumn().name
        val metadata = metadataMap[columnName]
        if (metadata == null) {
            metadataMap[columnName] = ColumnMetadata(unsigned = true)
        } else {
            metadataMap[columnName] = metadata.copy(unsigned = true)
        }

        return this
    }

    // todo: allow to pass `isUnique` param? as an autoincrement must be defined as a key
    fun <T : Number> ColumnRegistration<T>.autoIncrement(): ColumnRegistration<T> {
        val columnName = getColumn().name
        val metadata = metadataMap[columnName]
        if (metadata == null) {
            metadataMap[columnName] = ColumnMetadata(autoIncrement = true)
        } else {
            metadataMap[columnName] = metadata.copy(autoIncrement = true)
        }

        return this
    }

    fun <T : Any> ColumnRegistration<T>.nullable(): ColumnRegistration<T> {
        val columnName = getColumn().name
        val metadata = metadataMap[columnName]
        if (metadata == null) {
            metadataMap[columnName] = ColumnMetadata(nullable = true)
        } else {
            metadataMap[columnName] = metadata.copy(nullable = true)
        }

        return this
    }

    fun <T : Number> ColumnRegistration<T>.precision(total: Int, places: Int): ColumnRegistration<T> {
        when (getColumn().sqlType) {
            !is FloatSqlType, !is DoubleSqlType, !is DecimalSqlType -> return this
        }
        val columnName = getColumn().name
        val metadata = metadataMap[columnName]
        if (metadata == null) {
            metadataMap[columnName] = ColumnMetadata(precision = Precision(total, places))
        } else {
            metadataMap[columnName] = metadata.copy(precision = Precision(total, places))
        }

        return this
    }

    fun <T : Any> ColumnRegistration<T>.unique(): ColumnRegistration<T> {
        val columnName = getColumn().name
        val metadata = metadataMap[columnName]
        if (metadata == null) {
            metadataMap[columnName] = ColumnMetadata(unique = true)
        } else {
            metadataMap[columnName] = metadata.copy(unique = true)
        }

        return this
    }

    fun <T : Any> ColumnRegistration<T>.index(): ColumnRegistration<T> {
        val columnName = getColumn().name
        val metadata = metadataMap[columnName]
        if (metadata == null) {
            metadataMap[columnName] = ColumnMetadata(index = true)
        } else {
            metadataMap[columnName] = metadata.copy(index = true)
        }

        return this
    }

    /**
     * Define a created_at column typed of [InstantSqlType].
     */
    fun createdAt(name: String = "created_at"): ColumnRegistration<Instant> {
        return bindTimestamp(name)
    }

    /**
     * Define a updated_at column typed of [InstantSqlType].
     */
    fun updatedAt(name: String = "updated_at"): ColumnRegistration<Instant> {
        return bindTimestamp(name)
    }

    /**
     * Define a updated_at column typed of [InstantSqlType].
     */
    private fun bindTimestamp(name: String): ColumnRegistration<Instant> {
        val entityClass = this.entityClass ?: error("No entity class configured for table: $tableName")
        return registerColumn(name, InstantSqlType).nullable().apply {
            val colName = name.toCamelCase()
            val props = entityClass.memberProperties.find { prop ->
                prop.name == colName
            } ?: throw IllegalStateException("Entity ${entityClass.simpleName} doesn't contain property $colName.")
            doBindInternal(NestedBinding(listOf(props)))
        }
    }
}

internal data class ColumnMetadata(
    val size: Int? = null,
    val defaultValue: Any? = null,
    val unsigned: Boolean = false,
    val autoIncrement: Boolean = false,
    val nullable: Boolean = false,
    val unique: Boolean = false,
    val index: Boolean = true,
    val precision: Precision? = null,
    val useCurrentTimestamp: Boolean = false
)

internal data class Precision(val total: Int = 8, val places: Int = 2)
