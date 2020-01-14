package dev.alpas.runner.configs

import dev.alpas.Environment
import dev.alpas.auth.AuthConfig
import dev.alpas.auth.SessionAuthChannel
import dev.alpas.runner.Users

class AuthConfig(env: Environment) : AuthConfig(env) {
    init {
        addChannel("session") { call -> SessionAuthChannel(call, Users) }
    }
}
