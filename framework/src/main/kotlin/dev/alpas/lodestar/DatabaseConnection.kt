package dev.alpas.lodestar

import me.liuwj.ktorm.database.Database

interface DatabaseConnection {
    fun connect(): Database
}
