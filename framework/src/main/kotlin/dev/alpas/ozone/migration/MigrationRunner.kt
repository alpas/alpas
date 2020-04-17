package dev.alpas.ozone.migration

import com.github.ajalt.clikt.output.TermUi.echo
import com.github.ajalt.mordant.TermColors
import dev.alpas.*
import dev.alpas.extensions.toPascalCase
import io.github.classgraph.ScanResult
import java.io.File

internal class MigrationRunner(
    private val migrationDirectory: File,
    private val isDryRun: Boolean,
    private val migrationClassesScanner: ScanResult,
    private val migrateByFilenames: Boolean,
    private val quiet: Boolean
) {
    private val adapter by lazy { DbAdapter.make(isDryRun, quiet) }
    private val migrationRepo by lazy { MigrationRepo(adapter) }
    private val migrationFiles by lazy {
        (migrationDirectory.listFiles() ?: emptyArray()).map { it.nameWithoutExtension }
    }

    fun migrate() {
        val migrations = migrationsToRun().sortedBy { it.name }
        if (migrations.isEmpty()) {
            notify {
                echo(terminalColors.blue("✓ Everything is already migrated!"))
            }
            return
        }

        for (migration in migrations) {
            val givenName = migration.givenName
            val batch = migrationRepo.nextBatch


            notify {
                echo("${yellow("Migrating")} ${brightYellow(givenName)}")
            }

            // we'll still "mark" the migration being done so that it won't try to run the migration again
            if (migration.shouldSkipBatch(batch)) {
                notify {
                    deleteLastLine()
                    echo("${brightYellow("▸| Skipped for batch $batch:")} ${brightRed(givenName)}")
                }
            } else {
                migration.up()
            }
            if (!isDryRun) {
                migrationRepo.saveMigration(givenName)
                notify {
                    deleteLastLine()
                    echo("${brightGreen("✓ Migrated")} ${brightYellow(givenName)}")
                }
            }
        }
    }

    fun rollback() {
        val (migrations, batch) = migrationsToRollback()
        if (migrations.isEmpty()) {
            notify {
                println()
                "Nothing to rollback!".printAsInfo()
            }
            return
        }
        migrations.forEach {
            notify {
                echo("${yellow("Rolling back")} ${brightYellow(it.givenName)}")
            }
            it.down()
            notify {
                deleteLastLine()
                echo("${brightGreen("✓ Rolled back")} ${brightYellow(it.givenName)}")
            }
        }
        if (!isDryRun && batch >= 1) {
            migrationRepo.removeBatch(batch)
        }
    }

    private fun migrationsToRun(): List<Migration> {
        return if (migrateByFilenames) {
            migrationsToRunByFilenames()
        } else {
            migrationsToRunByMigrationNames()
        }
    }

    private fun migrationsToRunByMigrationNames(): List<Migration> {
        val migrations = mutableListOf<Migration>()
        migrationClassesScanner.getSubclasses(Migration::class.java.name).filter { !it.isAbstract }.forEach { classInfo ->
            val migration = classInfo.load<Migration>()
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
        migrationClassesScanner.getSubclasses(Migration::class.java.name).filter { !it.isAbstract }.forEach { classInfo ->
            // derive name of the migration from the corresponding filename
            val filename = migrationFiles.firstOrNull { name -> name.toPascalCase().endsWith(classInfo.simpleName) }
            if (filename != null && !migrationRepo.isMigrated(filename)) {
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

        val migrations = if (migrateByFilenames) {
            migrationsToRollbackByFilenames(migrationNamesToRollback)
        } else {
            migrationsToRollbackByMigrationNames(migrationNamesToRollback)
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
        migrationClassesScanner.getSubclasses(Migration::class.java.name).filter { !it.isAbstract }.forEach { classInfo ->
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
        migrationClassesScanner.getSubclasses(Migration::class.java.name).filter { !it.isAbstract }.forEach { classInfo ->
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

    private fun notify(tc: TermColors.() -> Unit) {
        if (!(quiet || isDryRun)) {
            tc(terminalColors)
        }
    }
}

