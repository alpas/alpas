package dev.alpas.http

import dev.alpas.secureRandomString
import dev.alpas.session.CSRF_SESSION_KEY
import javax.servlet.http.HttpSessionEvent
import javax.servlet.http.HttpSessionListener

internal class SessionListener : HttpSessionListener {
    override fun sessionCreated(se: HttpSessionEvent) {
        // set token for csrf checking
        if (se.session.getAttribute(CSRF_SESSION_KEY) == null) {
            se.session.setAttribute(CSRF_SESSION_KEY, secureRandomString(40))
        }
    }

    override fun sessionDestroyed(se: HttpSessionEvent) {
        if (se.session.getAttribute(CSRF_SESSION_KEY) != null) {
            se.session.removeAttribute(CSRF_SESSION_KEY)
        }
    }
}
