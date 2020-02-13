package dev.alpas.ozone.migration

import com.github.ajalt.clikt.output.TermUi.echo
import dev.alpas.*
import dev.alpas.extensions.toPascalCase
import java.io.File

internal class MigrationRunner(
    private val migrationDirectory: File,
    private val isDryRun: Boolean,
    private val packageClassLoader: PackageClassLoader,
    private val migrateByNames: Boolean,
    quiet: Boolean
) {
    private val adapter by lazy { DbAdapter.make(isDryRun, quiet) }
    private val migrationRepo by lazy { MigrationRepo(adapter) }
    private val migrationFiles by lazy {
        (migrationDirectory.listFiles() ?: emptyArray()).map { it.nameWithoutExtension }
    }

    private val shouldTalk = !(quiet || isDryRun)

    fun migrate() {
        val migrations = migrationsToRun().sortedBy { it.name }
        if (shouldTalk && migrations.isEmpty()) {
            "✓ Everything is already migrated!".printAsInfo()
            return
        }
        migrations.forEach {
            if (shouldTalk) {
                terminalColors.apply {
                    echo("${yellow("Migrating")} ${brightYellow(it.givenName)}")
                }
            }
            it.up()
            if (!isDryRun) {
                migrationRepo.saveMigration(it.givenName)
                if (shouldTalk) {
                    terminalColors.apply {
                        deleteLastLine()
                        echo("${brightGreen("✓ Migrated")} ${brightYellow(it.givenName)}")
                    }
                }
            }
        }
    }

    fun rollback() {
        val (migrations, batch) = migrationsToRollback()
        if (shouldTalk && migrations.isEmpty()) {
            println()
            "Nothing to rollback!".printAsInfo()
            return
        }
        migrations.forEach {
            if (shouldTalk) {
                terminalColors.apply {
                    echo("${yellow("Rolling back")} ${brightYellow(it.givenName)}")
                }
            }
            it.down()
            if (shouldTalk) {
                terminalColors.apply {
                    deleteLastLine()
                    echo("${brightGreen("✓ Rolled back")} ${brightYellow(it.givenName)}")
                }
            }
        }
        if (!isDryRun && batch >= 1) {
            migrationRepo.removeBatch(batch)
        }
    }

    private fun migrationsToRun(): List<Migration> {
        return if (migrateByNames) {
            migrationsToRunByMigrationNames()
        } else {
            migrationsToRunByFilenames()
        }
    }

    private fun migrationsToRunByMigrationNames(): List<Migration> {
        val migrations = mutableListOf<Migration>()
        packageClassLoader.classesExtending(Migration::class) {
            val migration = it.load<Migration>()
            val name = migration.name
            if (name != null && !migrationRepo.isMigrated(name)) {
                migration.givenName = name
                migration.adapter = adapter
                migrations.add(migration)
            }
        }
        return migrations
    }

    private fun migrationsToRunByFilenames(): List<Migration> {
        val migrations = mutableListOf<Migration>()
        packageClassLoader.classesExtending(Migration::class) { classInfo ->
            // derive name of the migration from the corresponding filename
            val filename = migrationFiles.first { name -> name.toPascalCase().endsWith(classInfo.simpleName) }
            if (!migrationRepo.isMigrated(filename)) {
                classInfo.load<Migration>().also { migration ->
                    migration.givenName = filename
                    migration.adapter = adapter
                    migrations.add(migration)
                }
            }
        }
        return migrations
    }

    private fun migrationsToRollback(): Pair<List<Migration>, Int> {
        // fetch the migrations that were added in the last batch
        val (latestBatchMigrations, batch) = migrationRepo.latestMigrationBatch()
        val migrationNamesToRollback = latestBatchMigrations.map { it.name }

        val migrations = if (migrateByNames) {
            migrationsToRollbackByMigrationNames(migrationNamesToRollback)
        } else {
            migrationsToRollbackByFilenames(migrationNamesToRollback)
        }

        val filenamesToRollback = migrations.map { it.givenName }
        migrationNamesToRollback.forEach {
            if (!filenamesToRollback.contains(it)) {
                // this means that the corresponding file wasn't found
                "Migration not found: $it".printAsError()
            }
        }
        return Pair(migrations, batch ?: 0)
    }

    private fun migrationsToRollbackByMigrationNames(migrationNamesToRollback: List<String>): List<Migration> {
        val migrations = mutableListOf<Migration>()
        packageClassLoader.classesExtending(Migration::class) { classInfo ->
            val migration = classInfo.load<Migration>()
            // If this migration is in the list of migrationNamesToRollback, we need to mark it for rolling back
            val name = migrationNamesToRollback.firstOrNull { name -> migration.name != null && name == migration.name }
            if (name != null) {
                migration.givenName = name
                migration.adapter = adapter
                migrations.add(migration)
            }
        }
        return migrations
    }

    private fun migrationsToRollbackByFilenames(migrationNamesToRollback: List<String>): List<Migration> {
        // mark all the migration files that contain migration classes. These migrations will be removed.
        val migrationFilesToRollback = migrationFiles.filter { migrationNamesToRollback.contains(it) }
        val migrations = mutableListOf<Migration>()
        packageClassLoader.classesExtending(Migration::class) { classInfo ->
            // check if this class is in the list of migrations that are marked to be removed
            val filename =
                migrationFilesToRollback.firstOrNull { name -> name.toPascalCase().endsWith(classInfo.simpleName) }
            if (filename != null) {
                classInfo.load<Migration>().also { migration ->
                    migration.givenName = filename
                    migration.adapter = adapter
                    migrations.add(migration)
                }
            }
        }
        return migrations
    }
}

