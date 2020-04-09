package dev.alpas.routing

internal fun combinePaths(path: String, vararg paths: String): String {
    return listOf(path, *paths).filter { it.isNotEmpty() }.joinToString("/").replace(Regex("/{2,}"), "/")
}

internal fun combineNames(name: String, vararg names: String): String {
    return listOf(name, *names)
        .filter { it.isNotBlank() }.joinToString(".")
        .replace(Regex(""".?<[0-9a-zA-Z]*>"""), "")
        .replace("..", ".")
}
