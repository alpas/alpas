package dev.alpas.ozone.migration

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

internal class SqliteAdapter(isDryRun: Boolean, quiet: Boolean, db: Database) : DbAdapter(isDryRun, quiet, db) {

    override fun execute(sql: String): Boolean {
        return if (isDryRun) {
            super.execute(sql)
        } else {
            transaction(Connection.TRANSACTION_SERIALIZABLE, 2) {
                execute(sql)
            }
        }
    }
}
