package dev.alpas.session

import dev.alpas.Application
import dev.alpas.ServiceProvider

class SessionServiceProvider : ServiceProvider {
    override fun register(app: Application) {
        app.singleton(SessionManager(app))
        app.env.supportsSession = true
    }
}
