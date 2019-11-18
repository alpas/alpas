package dev.alpas.auth

import dev.alpas.config
import dev.alpas.hashing.Hasher
import dev.alpas.http.HttpCall
import dev.alpas.make
import dev.alpas.session.SessionConfig

class SessionAuthChannel(private val call: HttpCall, override val userProvider: UserProvider) : AuthChannel {
    private val config = call.config { AuthConfig(call.make()) }
    private val sessionKey = config.authSessionKey
    override var user: Authenticatable? = null

    override fun tryLogin(): Boolean {
        if (isLoggedIn()) {
            return true
        }
        val id: Any? = call.session[sessionKey]
        if (id != null) {
            userProvider.findByPrimaryKey(id)?.also {
                user = it
            }
        }
        return isLoggedIn()
    }

    override fun attempt(id: String, password: String): Boolean {
        val user = validate(id, password)
        if (user != null) {
            login(user)
        }
        return user != null
    }

    private fun validate(id: Any, password: String): Authenticatable? {
        val user = userProvider.findByUsername(id)?.let { authenticatable ->
            val hasher = call.make<Hasher>()
            if (hasher.verify(password, authenticatable.password)) {
                authenticatable
            } else {
                // todo: throw exception if password is invalid
                null
            }
        }
        // todo: throw exception if user is null
        return user
    }

    override fun login(user: Authenticatable): Authenticatable {
        // todo: set remember token
        // todo: invalidate current session
        // todo: invalidate cookie session
        call.session[sessionKey] = user.id
        this.user = user
        return user
    }

    override fun loginUsingId(id: Any): Authenticatable? {
        return userProvider.findByPrimaryKey(id)?.also {
            login(it)
        }
    }

    override fun logout() {
        // todo: clear remember me cookie
        user = null
        val config = call.config<SessionConfig>()
        call.cookie.forget(config.cookieName, path = config.path, domain = config.domain)
        call.session.invalidate()
    }
}
