package dev.alpas.ozone.migration

import dev.alpas.ozone.OzoneEntity
import dev.alpas.ozone.OzoneTable
import dev.alpas.ozone.increments
import me.liuwj.ktorm.dsl.delete
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.dsl.max
import me.liuwj.ktorm.entity.*
import me.liuwj.ktorm.schema.int
import me.liuwj.ktorm.schema.varchar

private const val MIGRATION_TABLE = "migrations"
private const val NAME_COLUMN = "name"
private const val BATCH_COLUMN = "batch"

internal class MigrationRepo(private val dbAdapter: DbAdapter) {
    init {
        setupMigrationsTable()
    }

    private val migrations by lazy { Migrations.findAll() }

    private fun setupMigrationsTable() {
        CreateMigrationMigrations().apply {
            adapter = dbAdapter
            up()
        }
    }

    internal val nextBatch by lazy { (migrations.lastOrNull()?.batch ?: 0) + 1 }

    fun isMigrated(migration: String): Boolean {
        return migrations.firstOrNull { it.name == migration } != null
    }

    fun saveMigration(migration: String) {
        MigrationEntity {
            name = migration
            batch = nextBatch
        }.also {
            Migrations.add(it)
        }
    }

    fun latestMigrationBatch(): Pair<List<MigrationEntity>, Int?> {
        // get the max batch number
        val batch = Migrations.asSequenceWithoutReferences().aggregateColumns {
            max(
                Migrations.batch
            )
        }
        // find all the migrations from the above batch number
        val migrations = batch?.let { latestVersion ->
            Migrations.findList { Migrations.batch eq latestVersion }
        } ?: listOf()
        return Pair(migrations, batch)
    }

    fun removeBatch(batch: Int) {
        Migrations.delete { Migrations.batch eq batch }
    }

    internal interface MigrationEntity : OzoneEntity<MigrationEntity> {
        val id: Int
        var name: String
        var batch: Int

        companion object : OzoneEntity.Of<MigrationEntity>()
    }

    internal object Migrations : OzoneTable<MigrationEntity>(MIGRATION_TABLE) {
        val id by increments()
        val migration by varchar(NAME_COLUMN).bindTo { it.name }
        val batch by int(BATCH_COLUMN).bindTo { it.batch }
    }
}

internal class CreateMigrationMigrations : Migration() {
    override fun up() {
        if (adapter is PostgreSqlAdapter) {

            val query = """
            CREATE TABLE IF NOT EXISTS "migrations"(
                "id" serial4 not null,
                "name" varchar(255) not null,
                "batch" int not null,
                primary key ("id")
            )
        """.trimIndent()

            execute(query)
        } else {
            createTable(MigrationRepo.Migrations, true)
        }
    }
}
