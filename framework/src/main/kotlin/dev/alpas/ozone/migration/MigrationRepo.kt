package dev.alpas.ozone.migration

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

internal class MigrationRepo() {
    init {
        CreateMigrationMigrations().up()
    }

    private val migrations by lazy { MigrationRecord.all() }

    private val nextBatch by lazy { (migrations.lastOrNull()?.batch ?: 0) + 1 }

    fun isMigrated(migration: String): Boolean {
        return migrations.firstOrNull { it.name == migration } != null
    }

    fun saveMigration(migration: String) {
        MigrationRecords.insert {
            it[name] = migration
            it[batch] = nextBatch
        }
    }

    fun latestMigrationBatch(): Pair<List<MigrationRecord>, Int?> {
        // get the max batch number
        val query = MigrationRecords
            .slice(MigrationRecords.batch)
            .selectAll()
            .maxBy { it[MigrationRecords.batch] } ?: return Pair(emptyList(), null)

        val batch = query[MigrationRecords.batch]

        // find all the migrations from the above batch number
        val migrations = MigrationRecord.find { MigrationRecords.batch eq batch }.toList()

        return Pair(migrations, batch)
    }

    fun removeBatch(batch: Int) {
        MigrationRecords.deleteWhere { MigrationRecords.batch eq batch }
    }
}

internal class CreateMigrationMigrations : Migration() {
    override fun up() {
        createTable(MigrationRecords)
    }
}

class MigrationRecord(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<MigrationRecord>(MigrationRecords)

    var name by MigrationRecords.name
    var batch by MigrationRecords.batch
}

internal object MigrationRecords : IntIdTable("migrations") {
    val name = varchar("name", 255)
    val batch = integer("batch")
}
