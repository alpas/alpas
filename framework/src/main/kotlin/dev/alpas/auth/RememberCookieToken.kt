package dev.alpas.auth

data class RememberCookieToken(val token: String) {
    private val parsedTokens = token.split(':')

    fun id(): Any {
        return parsedTokens[0].toLong()
    }

    fun token(): String {
        return parsedTokens[1]
    }

    fun passwordHash(): String {
        return parsedTokens[2]
    }

    companion object {
        operator fun invoke(user: Authenticatable): String {
            return "${user.id}:${user.rememberToken}:${user.password}"
        }
    }
}
