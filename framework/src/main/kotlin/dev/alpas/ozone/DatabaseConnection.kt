package dev.alpas.ozone

import me.liuwj.ktorm.database.Database

interface DatabaseConnection {
    fun connect(): Database
    fun combineParams(params: Map<String, Any?>): String {
        return params
            .map { "${it.key}=${it.value}" }
            .joinToString("&")
    }

    fun disconnect()
    fun isClosed() : Boolean {
        return false
    }
    fun reconnect(): Database
}
