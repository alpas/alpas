package dev.alpas.validation

import dev.alpas.JsonSerializer
import uy.klutter.core.collections.toImmutable

class SharedDataBag : Iterable<Any> {
    private val contexts = mutableMapOf<String, Any?>()

    internal fun add(pair: Pair<String, Any?>, vararg pairs: Pair<String, Any>) {
        contexts[pair.first] = pair.second
        contexts.putAll(pairs)
    }

    fun all() = contexts.toImmutable()

    override fun iterator(): Iterator<Any> {
        return contexts.iterator()
    }

    operator fun get(key: String): Any? {
        return contexts[key]
    }

    internal operator fun set(key: String, value: Any?) {
        contexts[key] = value
    }
}

fun SharedDataBag.asJson(): String {
    return JsonSerializer.serialize(all())
}
