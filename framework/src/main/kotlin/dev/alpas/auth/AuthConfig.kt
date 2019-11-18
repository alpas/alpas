package dev.alpas.auth

import dev.alpas.Config
import dev.alpas.Environment
import dev.alpas.http.HttpCall
import java.time.Duration
import java.util.concurrent.atomic.AtomicReference

typealias AuthChannelFactory = (call: HttpCall) -> AuthChannel
typealias AuthChannelFilter = (authChannel: AuthChannel) -> Unit

open class AuthConfig(env: Environment) : Config {
    private val channels = mutableMapOf<String, AuthChannelFactory>()
    open val authSessionKey = "alpas-auth"
    open fun ifUnauthorizedRedirectToPath(call: HttpCall) = "/login"
    open fun ifAuthorizedRedirectToPath(call: HttpCall) = "/"
    open val defaultAuthChannel = env("AUTH_CHANNEL", "session")
    open val passwordResetTokenExpiration: Duration = Duration.ofMinutes(120)
    open val emailVerificationExpiration: Duration = Duration.ofMinutes(120)
    open var channelFilter: AtomicReference<AuthChannelFilter> = AtomicReference()

    fun addChannel(name: String, channelFactory: AuthChannelFactory) {
        channels[name] = channelFactory
    }

    open fun channel(call: HttpCall, key: String? = null): AuthChannel {
        return channels[key ?: defaultAuthChannel]?.invoke(call)?.also {
            channelFilter.get()?.invoke(it)
        } ?: throw Exception("No such auth channel: '$key'.")
    }

    open fun userProvider(call: HttpCall, key: String = defaultAuthChannel): UserProvider {
        return channel(call, key).userProvider ?: throw Exception("No user provider registered for channel: '$key'.")
    }
}
