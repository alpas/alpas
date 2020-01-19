package dev.alpas.logging

import dev.alpas.Application
import dev.alpas.ServiceProvider
import dev.alpas.appConfig
import java.nio.file.Paths

open class LoggerServiceProvider : ServiceProvider {
    override fun register(app: Application) {
        val config = app.appConfig()
        val logConfig = if (app.env.runMode.isConsole()) config.consoleLogConfig else config.appLogConfig
        System.setProperty(
            "logback.configurationFile", Paths.get(app.env.rootDir, logConfig).toAbsolutePath().toString()
        )
    }
}

