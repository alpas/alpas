package dev.alpas.pulsar

import me.liuwj.ktorm.entity.Entity

interface EntityFactory<E : Entity<*>> {
    fun makeMany(count: Int = 1, attrs: Map<String, Any?> = emptyMap()): List<E> {
        return (1..count).map { make(attrs) }
    }

    fun make(attrs: Map<String, Any?> = emptyMap()): E {
        return this(attrs)
    }

    operator fun invoke(attrs: Map<String, Any?> = emptyMap()): E
}
