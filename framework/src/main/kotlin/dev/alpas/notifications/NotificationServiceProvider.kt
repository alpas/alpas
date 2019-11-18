package dev.alpas.notifications

import dev.alpas.Application
import dev.alpas.ServiceProvider

class NotificationServiceProvider : ServiceProvider {
    override fun register(app: Application) {
        app.bufferDebugLog("Registering $this")
        app.bind(NotificationDispatcher(app))
    }
}
