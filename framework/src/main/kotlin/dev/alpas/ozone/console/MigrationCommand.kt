package dev.alpas.ozone.console

import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import dev.alpas.PackageClassLoader
import dev.alpas.console.Command
import java.nio.file.Path
import java.nio.file.Paths

abstract class MigrationCommand(protected val srcPackage: String, name: String, help: String) :
    Command(name = name, help = help) {

    protected val useNames by option(
        "--use-names",
        "-n",
        help = "Migrate using migration names rather than filenames"
    ).flag(default = false)
    protected val dryRun by option("--dry-run", "-d", help = "Don't execute the query but only print it").flag()
    protected val migrationsDirectory: Path by lazy { Paths.get(srcDir, "database", "migrations") }
    protected val packageClassLoader: PackageClassLoader by lazy { PackageClassLoader(srcPackage) }
}
