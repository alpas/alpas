package dev.alpas.ozone

import org.jetbrains.exposed.sql.Database

/**
 * Checks whether this database is of the given name type.
 */
fun Database.isOfType(name: String): Boolean {
    return this.vendor.toLowerCase() == name.toLowerCase()
}

/**
 * Checks whether this database is of type MySQL.
 */
fun Database.isMySql(): Boolean {
    return isOfType("mysql")
}

/**
 * Checks whether this database is of type SQLite.
 */
fun Database.isSqlite(): Boolean {
    return isOfType("sqlite")
}
