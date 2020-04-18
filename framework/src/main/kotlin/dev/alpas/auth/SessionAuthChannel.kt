package dev.alpas.auth

import dev.alpas.config
import dev.alpas.hashing.Hasher
import dev.alpas.http.HttpCall
import dev.alpas.make
import dev.alpas.secureRandomString
import dev.alpas.session.SessionConfig
import dev.alpas.sha1

@Suppress("unused")
open class SessionAuthChannel(private val call: HttpCall, override val userProvider: UserProvider) : AuthChannel {
    private val sessionKey = call.config { AuthConfig(call.make()) }.authSessionKey

    override var user: Authenticatable? = null
    override fun tryLogin(): Boolean {
        if (isLoggedIn()) {
            return true
        }
        val id = call.session.get<Any>(sessionKey)
        if (id != null) {
            userProvider.findByPrimaryKey(id)?.also {
                user = it
            }
        }
        if (!isLoggedIn()) {
            // If the user is still not logged in, let's try to login using the "Remember Token"
            tryLoginUsingRememberToken()
        }
        return isLoggedIn()
    }

    override fun attempt(id: String, password: String, remember: Boolean): Boolean {
        return validate(id, password)?.let {
            login(it, remember)
            true
        } ?: false
    }

    override fun login(user: Authenticatable, remember: Boolean) {
        // todo: invalidate cookie session
        if (remember) {
            refreshRememberToken(user)
            setRememberTokenCookie(user)
        }
        setSession(user)
        this.user = user
    }

    protected open fun rememberTokenCookieName(): String {
        val hash = this.javaClass.simpleName.sha1()
        return "remember_me_session_${hash}"
    }

    override fun loginUsingId(id: Any): Authenticatable? {
        return userProvider.findByPrimaryKey(id)?.also {
            login(it)
        }
    }

    override fun logout() {
        user?.let {
            // If user had a "Remember Token" set previously, we'll just refresh it to invalidate the
            // token altogether so that the token on the other devices are invalidated as well.
            if (it.rememberToken != null) {
                refreshRememberToken(it)
            }
        }
        forgetRememberCookie()
        user = null
        val sessionConfig = call.config<SessionConfig>()
        call.cookie.forget(sessionConfig.cookieName, path = sessionConfig.path, domain = sessionConfig.domain)
        call.session.invalidate()
    }

    private fun tryLoginUsingRememberToken() {
        call.cookie[rememberTokenCookieName()]?.let { cookie ->
            val rememberToken = RememberCookieToken(cookie)
            if (rememberToken.isValid()) {
                user = userProvider.findByToken(rememberToken)?.also {
                    setSession(it)
                }
            }
        }
    }

    private fun validate(id: Any, password: String): Authenticatable? {
        val user = userProvider.findByUsername(id)?.let { authenticatable ->
            val hasher = call.make<Hasher>()
            if (hasher.verify(password, authenticatable.password)) {
                authenticatable
            } else {
                // todo: throw exception if password is invalid?
                null
            }
        }
        // todo: throw exception if user is null?
        return user
    }

    private fun refreshRememberToken(user: Authenticatable) {
        if (user.rememberToken.isNullOrEmpty()) {
            val token = secureRandomString(60)
            userProvider.updateRememberToken(user, token)
        }
    }

    private fun setRememberTokenCookie(user: Authenticatable) {
        val name = rememberTokenCookieName()
        val value = RememberCookieToken(user)
        val cookie = call.cookie.immortal(name, value)
        call.cookie.add(cookie)
    }

    private fun forgetRememberCookie() {
        call.cookie.forget(rememberTokenCookieName())
    }

    private fun setSession(user: Authenticatable) {
        call.session[sessionKey] = user.id
    }
}
