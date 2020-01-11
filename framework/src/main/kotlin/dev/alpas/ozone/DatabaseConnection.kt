package dev.alpas.ozone

import org.jetbrains.exposed.sql.Database

interface DatabaseConnection {
    fun connect(): Database
}
