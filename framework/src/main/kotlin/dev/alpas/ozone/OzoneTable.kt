package dev.alpas.ozone

import dev.alpas.extensions.toCamelCase
import me.liuwj.ktorm.dsl.AssignmentsBuilder
import me.liuwj.ktorm.dsl.combineConditions
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.dsl.isNull
import me.liuwj.ktorm.entity.Entity
import me.liuwj.ktorm.entity.findOne
import me.liuwj.ktorm.expression.ArgumentExpression
import me.liuwj.ktorm.expression.ColumnAssignmentExpression
import me.liuwj.ktorm.schema.*
import java.time.Instant
import java.time.temporal.Temporal
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

abstract class OzoneTable<E : Ozone<E>>(tableName: String, alias: String? = null, entityClass: KClass<E>? = null) :
    Table<E>(tableName, alias, entityClass) {

    protected open val autoBindColumnNames = arrayOf("id", "created_at", "updated_at")

    internal val metadataMap = hashMapOf<String, ColumnMetadata>()

    open fun shouldAutoBind(column: String): Boolean {
        return autoBindColumnNames.contains(column)
    }

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
    fun createdAt(
        name: String = "created_at",
        nullable: Boolean = true,
        useCurrent: Boolean = true
    ): ColumnRegistration<Instant> {
        return registerAndBind(name, InstantSqlType).apply {
            if (nullable) {
                nullable()
            }
            if (useCurrent) {
                useCurrent()
            }
        }
    }

    /**
     * Define a updated_at column typed of [InstantSqlType].
     */
    fun updatedAt(
        name: String = "updated_at",
        nullable: Boolean = true,
        useCurrent: Boolean = true
    ): ColumnRegistration<Instant> {
        return registerAndBind(name, InstantSqlType).apply {
            if (nullable) {
                nullable()
            }
            if (useCurrent) {
                useCurrent()
            }
        }
    }

    /**
     * Automatically register and bind a column under the given name and type.
     */
    internal fun <T : Any> registerAndBind(name: String, type: SqlType<T>): ColumnRegistration<T> {
        return registerColumn(name, type).also {
            if (shouldAutoBind(name)) {
                autoBind(it)
            }
        }
    }

    /**
     * Automatically bind a column after inferring its corresponding member name from the entity.
     */
    private fun <T : Any> autoBind(column: ColumnRegistration<T>): ColumnRegistration<T> {
        val entityClass = this.entityClass ?: error("No entity class configured for table: $tableName")
        return column.apply {
            val colName = columnName.toCamelCase()
            val props = entityClass.memberProperties.find { prop ->
                prop.name == colName
            } ?: throw IllegalStateException("Entity ${entityClass.simpleName} doesn't contain property $colName.")
            doBindInternal(NestedBinding(listOf(props)))
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun findOrCreate(whereAttributes: Map<String, Any?>, attributes: Map<String, Any?> = emptyMap()): E {
        if (whereAttributes.isEmpty()) {
            throw IllegalArgumentException("whereAttributes cannot be empty.")
        }
        val table = this
        val whereConditions = whereAttributes.map {
            val col = table[it.key] as Column<Any>
            when (val value = it.value) {
                null -> col.isNull()
                else -> col.eq(value)
            }
        }
        return findOne { whereConditions.combineConditions() }
            ?: create { builder ->
                val allAttributes = whereAttributes.plus(attributes)
                for ((name, value) in allAttributes) {
                    table.columns.find { it.name == name }?.let {
                        builder[name] to value
                    }
                }
            }
    }

    @Suppress("UNCHECKED_CAST")
    fun create(attributes: Map<String, Any?>): E {
        val table = this
        return create { builder ->
            for ((name, value) in attributes) {
                table.columns.find { it.name == name }?.let {
                    builder[name] to value
                }
            }
        }
    }
}

fun <E : Ozone<E>, T : OzoneTable<E>> T.create(
    attributes: Map<String, Any?> = emptyMap(),
    block: AssignmentsBuilder.(T) -> Unit
): E {
    val combinedAttributes = attributes.toMutableMap()
    val assignments = ArrayList<ColumnAssignmentExpression<*>>()
    AssignmentsBuilder(assignments).block(this)
    assignments.map {
        combinedAttributes[it.column.name] = (it.expression as ArgumentExpression).value
    }
    return create(combinedAttributes)
}

/**
 * A map of an entity's column name to the actual corresponding column name in the table.
 */
fun <T : Entity<T>> Table<T>.propertyNamesToColumnNames(): Map<String, String> {
    return columns.map { col ->
        val colNameInTable = when (val binding = col.binding) {
            is NestedBinding -> {
                binding.properties[0].name
            }
            else -> col.name
        }
        Pair(colNameInTable, col.name)
    }.toMap()
}

data class ColumnMetadata(
    val size: Int? = null,
    val defaultValue: Any? = null,
    val unsigned: Boolean = false,
    val autoIncrement: Boolean = false,
    val nullable: Boolean = false,
    val unique: Boolean = false,
    val index: Boolean = true,
    val precision: Precision? = null,
    val useCurrentTimestamp: Boolean = false,
    internal val after: String? = null
)

data class Precision(val total: Int = 8, val places: Int = 2)
