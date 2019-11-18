package dev.alpas.cookie

import java.time.Duration
import javax.servlet.http.Cookie

class CookieJar(private val incomingCookies: Array<Cookie>) : Iterable<Cookie> {
    private val outgoingCookies = mutableListOf<Cookie>()

    override fun iterator(): Iterator<Cookie> {
        return outgoingCookies.iterator()
    }

    operator fun get(name: String): String? {
        return incomingCookies.firstOrNull {
            it.name == name
        }?.value
    }

    operator fun set(name: String, value: String?) {
        incomingCookies.firstOrNull {
            it.name == name
        }?.value = value

        outgoingCookies.firstOrNull {
            it.name == name
        }?.value = value
    }

    fun add(
        name: String,
        value: String?,
        lifetime: Duration = Duration.ofSeconds(-1),
        path: String? = null,
        domain: String? = null,
        secure: Boolean = false,
        httpOnly: Boolean = true
    ): CookieJar {
        add(make(name, value, lifetime, path, domain, secure, httpOnly))
        return this
    }

    fun add(cookie: Cookie): CookieJar {
        outgoingCookies.add(cookie)
        return this
    }

    fun make(
        name: String,
        value: String?,
        lifetime: Duration = Duration.ofSeconds(-1),
        path: String? = null,
        domain: String? = null,
        secure: Boolean = false,
        httpOnly: Boolean = true
    ): Cookie {
        return Cookie(name, value).also { cookie ->
            cookie.maxAge = lifetime.seconds.toInt()
            path?.let { cookie.path = it }
            domain?.let { cookie.domain = it }
            cookie.secure = secure
            cookie.isHttpOnly = httpOnly
        }
    }

    fun forget(name: String, path: String? = null, domain: String? = null): CookieJar {
        return add(name, "", lifetime = Duration.ZERO, path = path, domain = domain)
    }
}
