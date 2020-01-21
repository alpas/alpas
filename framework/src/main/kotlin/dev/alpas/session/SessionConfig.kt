package dev.alpas.session

import dev.alpas.Config
import dev.alpas.Environment
import java.nio.file.Paths
import java.time.Duration

open class SessionConfig(env: Environment) : Config {
    open val storeDriver: SessionStoreDriver by lazy {
        SessionStoreDriver.fromString(
            env("SESSION_DRIVER"),
            SessionStoreDriver.FILE
        )
    }

    open val httpOnly = true

    open val lifetime: Duration = Duration.ofHours(2)

    open val path = "/"

    open val domain = env("SESSION_DOMAIN")

    open val cookieName = "alpas_session"

    open val secure = env("SESSION_SECURE_COOKIE")?.toBoolean() ?: false

    open val cacheDriver by lazy {
        SessionCacheDriver.fromString(
            env("SESSION_CACHE_DRIVER"),
            SessionCacheDriver.MEMORY
        )
    }

    open val storePath: String = Paths.get(env.storagePath, "", "sessions").toAbsolutePath().toString()

    open val encryptExcept = listOf<String>()
}

enum class SessionStoreDriver {
    FILE, SKIP;

    companion object {
        fun fromString(name: String?, default: SessionStoreDriver): SessionStoreDriver {
            return name?.toUpperCase()?.let { valueOf(it) } ?: default
        }
    }
}

enum class SessionCacheDriver {
    MEMORY, SKIP;

    companion object {
        fun fromString(name: String?, default: SessionCacheDriver): SessionCacheDriver {
            return name?.toUpperCase()?.let { valueOf(it) } ?: default
        }
    }
}
