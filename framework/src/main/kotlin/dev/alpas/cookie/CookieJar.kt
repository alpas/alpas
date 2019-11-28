package dev.alpas.cookie

import javax.servlet.http.Cookie

class CookieJar(private val incomingCookies: Array<Cookie>) : Iterable<Cookie> {
    private val outgoingCookies = mutableListOf<Cookie>()

    override fun iterator(): Iterator<Cookie> {
        return outgoingCookies.iterator()
    }

    operator fun invoke(name: String) = get(name)

    operator fun get(name: String): String? {
        return incomingCookies.firstOrNull {
            it.name == name
        }?.value
    }
}
