package dev.alpas.routing

internal fun combinePaths(vararg paths: String): String {
    return paths.filter { it.isNotEmpty() }.joinToString("/").replace(Regex("/{2,}"), "/")
}

internal fun combineNames(vararg names: String): String {
    return names.filter { it.isNotBlank() }.joinToString(".").replace(Regex("/.{2,}"), ".")
}
