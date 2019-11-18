package dev.alpas.session

import dev.alpas.Config
import dev.alpas.Environment
import java.nio.file.Paths
import java.time.Duration

open class SessionConfig(env: Environment) : Config {
    open var storeDriver: SessionStoreDriver =
        SessionStoreDriver.fromString(
            env("SESSION_DRIVER"),
            SessionStoreDriver.FILE
        )
        protected set

    open var httpOnly = true
        protected set

    open var lifetime: Duration = Duration.ofHours(2)
        protected set

    open var path = "/"
        protected set

    open var domain = env("SESSION_DOMAIN")

    open var cookieName = "alpas_session"
        protected set

    open var secure = env("SESSION_SECURE_COOKIE")?.toBoolean() ?: false
        protected set

    open var cacheDriver = SessionCacheDriver.fromString(
        env("SESSION_CACHE_DRIVER"),
        SessionCacheDriver.MEMORY
    )
        protected set

    open var storePath: String = Paths.get(env.storagePath, "", "sessions").toUri().path
        protected set

    open var encryptExcept = listOf<String>()
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
