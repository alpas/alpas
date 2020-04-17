package dev.alpas.ozone.console

import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import dev.alpas.console.Command
import io.github.classgraph.ClassGraph
import io.github.classgraph.ScanResult
import java.nio.file.Path
import java.nio.file.Paths

abstract class MigrationCommand(protected val srcPackage: String, name: String, help: String) :
    Command(name = name, help = help) {

    protected val useFilenames by option(
        "--use-files",
        "-n",
        help = """Migrate using migration file names rather than the "name" property."""
    ).flag(default = false)
    protected val dryRun by option("--dry-run", "-d", help = "Don't execute the query but only print it").flag()
    protected val migrationsDirectory: Path by lazy { Paths.get(srcDir, "database", "migrations") }
    protected val migrationClassesScanner: ScanResult by lazy {
        ClassGraph().disableNestedJarScanning().enableClassInfo().scan()
    }
}
