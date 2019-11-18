package dev.alpas.ozone.migration

import dev.alpas.PackageClassLoader
import dev.alpas.extensions.toPascalCase
import dev.alpas.printAsError
import dev.alpas.printAsInfo
import dev.alpas.printAsSuccess
import dev.alpas.printAsWarning
import java.io.File
import kotlin.reflect.full.createInstance

internal class MigrationRunner(
    private val migrationDirectory: File,
    private val isDryRun: Boolean,
    private val packageClassLoader: PackageClassLoader
) {

    private val adapter by lazy { DbAdapter.make(isDryRun) }
    private val migrationRepo by lazy { MigrationRepo(adapter) }
    private val migrationFiles by lazy {
        (migrationDirectory.listFiles() ?: emptyArray()).map { it.nameWithoutExtension }
    }

    fun migrate() {
        val migrations = migrationsToRun()
        if (migrations.isEmpty()) {
            "Everything is already migrated!".printAsInfo()
            return
        }
        migrations.forEach {
            if (!isDryRun) {
                "Migrating: ${it.filename}".printAsWarning()
            }
            it.up()
            if (!isDryRun) {
                migrationRepo.saveMigration(it.filename)
                "Migrated: ${it.filename}".printAsSuccess()
            }
        }
    }

    fun rollback() {
        val (migrations, batch) = migrationsToRollback()
        if (migrations.isEmpty()) {
            "Nothing to rollback!".printAsInfo()
            return
        }
        migrations.forEach {
            if (!isDryRun) {
                "Rolling back: ${it.filename}".printAsWarning()
            }
            it.down()
            if (!isDryRun) {
                "Rolled back: ${it.filename}".printAsSuccess()
            }
        }
        if (!isDryRun && batch >= 1) {
            migrationRepo.removeBatch(batch)
        }
    }

    private fun migrationsToRun(): List<Migration> {
        val migrations = mutableListOf<Migration>()

        packageClassLoader.classesExtending(Migration::class) {
            // derive name of the migration from the corresponding filename
            val filename = migrationFiles.first { name -> name.toPascalCase().endsWith(it.simpleName) }
            if (!migrationRepo.isMigrated(filename)) {
                (it.loadClass().kotlin.createInstance() as Migration).also { migration ->
                    migration.filename = filename
                    migration.adapter = adapter
                    migrations.add(migration)
                }
            }
        }
        migrations.sortBy { it.filename }

        return migrations
    }

    private fun migrationsToRollback(): Pair<List<Migration>, Int> {
        val migrations = mutableListOf<Migration>()

        // fetch the migrations that were added in the last batch
        val (latestBatchMigrations, batch) = migrationRepo.latestMigrationBatch()
        val latestBatchMigrationNames = latestBatchMigrations.map { it.name }.toMutableList()
        // mark all the migration files that contain migration classes. These migrations will be removed.
        val migrationFilesToRemove = migrationFiles.filter { latestBatchMigrationNames.contains(it) }

        packageClassLoader.classesExtending(Migration::class) { classinfo ->
            // check if this class is in the list of migrations that are marked to be removed
            val filename =
                migrationFilesToRemove.firstOrNull { name -> name.toPascalCase().endsWith(classinfo.simpleName) }
            if (filename != null) {
                (classinfo.loadClass().kotlin.createInstance() as Migration).also { migration ->
                    migration.filename = filename
                    migration.adapter = adapter
                    migrations.add(migration)
                    latestBatchMigrationNames.remove(filename)
                }
            }
        }

        latestBatchMigrationNames.forEach {
            "Migration not found: $it".printAsError()
        }

        return if (latestBatchMigrationNames.isEmpty()) Pair(migrations, batch ?: 0) else return Pair(listOf(), 0)
    }
}

