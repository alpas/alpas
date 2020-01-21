package dev.alpas.cookie

import dev.alpas.*
import dev.alpas.encryption.Encrypter
import dev.alpas.http.HttpCall
import dev.alpas.session.SessionConfig
import javax.servlet.http.Cookie

class EncryptCookies : Middleware<HttpCall>() {
    override fun invoke(call: HttpCall, forward: Handler<HttpCall>) {
        val incomingCookies = cookiesToEncrypt(call, call.cookie.incomingCookies)
        val encrypter = call.make<Encrypter>()
        if (incomingCookies.count() > 0) {
            // decrypt incoming cookies
            incomingCookies.forEach {
                try {
                    it.value = encrypter.decrypt(it.value)
                } catch (e: Exception) {
                    it.value = ""
                }
            }
        }

        forward(call)

        val outgoingCookies = cookiesToEncrypt(call, call.cookie.outgoingCookies.toTypedArray())
        if (outgoingCookies.count() > 0) {
            // encrypt outgoing cookies
            outgoingCookies.forEach {
                it.value = encrypter.encrypt(it.value)
            }
        }
    }

    private fun cookiesToEncrypt(call: HttpCall, cookies: Array<Cookie>): Iterable<Cookie> {
        val config = call.makeElse { SessionConfig(call.env) }
        return cookies.filterNot {
            it.name.isOneOf(
                config.cookieName,
                "JSESSIONID",
                "X-CSRF-TOKEN",
                *config.encryptExcept.toTypedArray()
            )
        }
    }
}
