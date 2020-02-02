package dev.alpas.ozone.migration

import dev.alpas.printAsSuccess

internal class PostgreSQLAdapter(isDryRun: Boolean, quiet: Boolean) : DbAdapter(isDryRun, quiet) {
    override fun createTable(tableBuilder: TableBuilder, ifNotExists: Boolean) {
        TODO("Create table not yet supported for PostgreSQL")
    }

    override fun createDatabase(name: String): Boolean {
        return execute(""" CREATE DATABASE "$name" """.trim())
    }

    override fun dropTable(tableName: String) {
        execute(""" DROP TABLE "$tableName" """.trim())
    }

    override fun dropAllTables() {
        val query = """
                DROP SCHEMA public CASCADE; CREATE SCHEMA public; GRANT USAGE ON SCHEMA public to PUBLIC; GRANT CREATE ON SCHEMA public to PUBLIC; COMMENT ON SCHEMA public IS 'standard public schema';
            """.trimIndent()

        execute(query)

        if (shouldTalk) {
            "Done!".printAsSuccess()
        }
    }
}
