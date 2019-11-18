package dev.alpas.ozone

import me.liuwj.ktorm.database.Database

interface DatabaseConnection {
    fun connect(): Database
}
