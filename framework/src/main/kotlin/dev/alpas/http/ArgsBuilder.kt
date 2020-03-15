package dev.alpas.http

class ArgsBuilder(private val assignments: MutableMap<String, Any?> = mutableMapOf()) {
    infix fun String.to(argument: Any?) {
        assignments[this] = argument
    }

    internal fun map(): Map<String, Any?> {
        return assignments
    }
}
