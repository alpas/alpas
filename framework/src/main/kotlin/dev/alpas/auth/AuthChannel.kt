package dev.alpas.auth

interface AuthChannel {
    val userProvider: UserProvider? get() = null
    var user: Authenticatable?

    fun attempt(id: String, password: String): Boolean {
        return false
    }

    fun check(): Boolean {
        if (!isLoggedIn()) {
            tryLogin()
        }
        return isLoggedIn()
    }

    fun isLoggedIn(): Boolean {
        return user != null
    }

    fun tryLogin(): Boolean {
        return false
    }

    fun login(user: Authenticatable): Authenticatable {
        TODO("not implemented")
    }

    fun loginUsingId(id: Any): Authenticatable? {
        return null
    }

    fun logout() {
        TODO("not implemented")
    }
}
