package dev.alpas.validation

import dev.alpas.JsonSerializable
import dev.alpas.JsonSerializer
import dev.alpas.http.RequestError

class ErrorBag(vararg error: RequestError) : JsonSerializable {
    private val errors = mutableListOf(*error)
    fun add(requestError: RequestError) {
        errors.add(requestError)
    }

    fun all(): List<RequestError> {
        return errors
    }

    fun isEmpty(): Boolean {
        return errors.isEmpty()
    }

    fun asMap(): Map<String, List<String>> {
        val map = mutableMapOf<String, MutableList<String>>()
        all().forEach { error ->
            map[error.attribute] = (map[error.attribute] ?: mutableListOf()).also { it.add(error.message) }
        }
        return map
    }

    override fun toJson(): String {
        return JsonSerializer.serialize(asMap())
    }

    override fun toString(): String {
        return asMap().toString()
    }
}
