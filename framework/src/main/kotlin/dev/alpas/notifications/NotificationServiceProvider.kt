package dev.alpas.notifications

import dev.alpas.Application
import dev.alpas.ServiceProvider
import dev.alpas.console.Command
import dev.alpas.notifications.console.MakeNotificationCommand

class NotificationServiceProvider : ServiceProvider {
    override fun register(app: Application) {
        app.bufferDebugLog("Registering $this")
        app.bind(NotificationDispatcher(app))
    }

    override fun commands(app: Application): List<Command> {
        return listOf(
            MakeNotificationCommand(app.srcPackage)
        )
    }
}
