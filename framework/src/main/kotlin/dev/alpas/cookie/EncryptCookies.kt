package dev.alpas.cookie

import dev.alpas.*
import dev.alpas.encryption.Encrypter
import dev.alpas.http.HttpCall
import dev.alpas.session.SessionConfig
import javax.servlet.http.Cookie

class EncryptCookies : Middleware<HttpCall>() {
    override fun invoke(call: HttpCall, forward: Handler<HttpCall>) {
        val incomingCookies = cookiesToEncrypt(call)
        if (incomingCookies.count() > 0) {
            val encrypter = call.make<Encrypter>()
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

        val outgoingCookies = cookiesToEncrypt(call)
        if (outgoingCookies.count() > 0) {
            val encrypter = call.make<Encrypter>()
            // encrypt outgoing cookies
            outgoingCookies.forEach {
                it.value = encrypter.encrypt(it.value)
            }
        }
    }

    private fun cookiesToEncrypt(call: HttpCall): List<Cookie> {
        val config = call.config<SessionConfig>()
        return call.cookie.filterNot {
            it.name.isOneOf(
                config.cookieName,
                "JSESSIONID",
                "X-CSRF-TOKEN",
                *config.encryptExcept.toTypedArray()
            )
        }
    }
}
