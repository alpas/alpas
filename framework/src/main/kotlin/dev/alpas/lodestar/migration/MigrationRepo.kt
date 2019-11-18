package dev.alpas.lodestar.migration

import me.liuwj.ktorm.dsl.delete
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.dsl.max
import me.liuwj.ktorm.entity.Entity
import me.liuwj.ktorm.entity.add
import me.liuwj.ktorm.entity.aggregateColumns
import me.liuwj.ktorm.entity.asSequenceWithoutReferences
import me.liuwj.ktorm.entity.findAll
import me.liuwj.ktorm.entity.findList
import me.liuwj.ktorm.schema.Table
import me.liuwj.ktorm.schema.int
import me.liuwj.ktorm.schema.varchar

private const val MIGRATION_TABLE = "migrations"
private const val ID_COLUMN = "id"
private const val NAME_COLUMN = "name"
private const val BATCH_COLUMN = "batch"

internal class MigrationRepo(private val connection: ConnectionInterface) {
    init {
        setupMigrationsTable()
    }

    private val migrations by lazy { Migrations.findAll() }

    private fun setupMigrationsTable() {
        if (!connection.doesTableExist(MIGRATION_TABLE)) {
            connection.adapter.createTable(MIGRATION_TABLE) {
                increments(ID_COLUMN)
                varchar(NAME_COLUMN)
                unsignedInteger(BATCH_COLUMN)
            }
        }
    }

    private val nextBatch by lazy { (migrations.lastOrNull()?.batch ?: 0) + 1 }

    fun isMigrated(migration: String): Boolean {
        return migrations.firstOrNull { it.name == migration } != null
    }

    fun saveMigration(migration: String) {
        Migration {
            name = migration
            batch = nextBatch
        }.also {
            Migrations.add(it)
        }
    }

    fun latestMigrationBatch(): Pair<List<Migration>, Int?> {
        // get the max batch number
        val batch = Migrations.asSequenceWithoutReferences().aggregateColumns { max(it.batch) }
        // find all the migrations from the above batch number
        val migrations = batch?.let { latestVersion ->
            Migrations.findList { it.batch eq latestVersion }
        } ?: listOf()
        return Pair(migrations, batch)
    }

    fun removeBatch(batch: Int) {
        Migrations.delete { it.batch eq batch }
    }

    internal interface Migration : Entity<Migration> {
        companion object : Entity.Factory<Migration>()

        val id: Int
        var name: String
        var batch: Int
    }

    internal object Migrations : Table<Migration>(MIGRATION_TABLE) {
        val id by int(ID_COLUMN).primaryKey().bindTo { it.id }
        val migration by varchar(NAME_COLUMN).bindTo { it.name }
        val batch by int(BATCH_COLUMN).bindTo { it.batch }
    }
}
