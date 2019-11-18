package dev.alpas.http

import dev.alpas.secureRandomString
import dev.alpas.session.csrfSessionKey
import javax.servlet.http.HttpSessionEvent
import javax.servlet.http.HttpSessionListener

internal class SessionListener : HttpSessionListener {
    override fun sessionCreated(se: HttpSessionEvent) {
        // set token for csrf checking
        if (se.session.getAttribute(csrfSessionKey) == null) {
            se.session.setAttribute(csrfSessionKey, secureRandomString(40))
        }
    }

    override fun sessionDestroyed(se: HttpSessionEvent) {
        if (se.session.getAttribute(csrfSessionKey) != null) {
            se.session.removeAttribute(csrfSessionKey)
        }
    }
}
