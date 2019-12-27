package dev.alpas

import java.time.Duration
import java.time.ZoneOffset

open class AppConfig(env: Environment) : Config {
    open val enableNetworkShare = env("ENABLE_NETWORK_SHARE", false)
    open val appPort = env("APP_PORT", 8080)
    open val appUrl = env("APP_URL", "")
    open val connectionTimeOut: Duration = Duration.ofMinutes(1)
    open val staticDirs = arrayOf("web")
    open val encryptionKey = env("APP_KEY")
    open val maxThreads = env("APP_MAX_THREADS", 200)
    open val minThreads = env("APP_MIN_THREADS", 8)
    open val timezone: ZoneOffset = ZoneOffset.UTC
    open val appLogConfig: String = env("APP_LOG_CONFIG", "app_log_config.xml")
    open val consoleLogConfig: String = env("CONSOLE_LOG_CONFIG", "console_log_config.xml")
    open val commandAliases: Map<String, List<String>> = emptyMap()
    open val allowMethodSpoofing = true
}
