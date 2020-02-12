package dev.alpas.ozone.console

import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import dev.alpas.console.GeneratorCommand
import dev.alpas.console.OutputFile
import dev.alpas.extensions.toPascalCase
import dev.alpas.extensions.toSnakeCase
import dev.alpas.ozone.console.stubs.EntityStubs
import org.atteo.evo.inflector.English
import java.io.File

class MakeEntityCommand(srcPackage: String) :
    GeneratorCommand(
        srcPackage,
        name = "make:entity",
        help = "Create an entity class with a corresponding entity table"
    ) {

    private val tableName by option("--table", help = "Name of the table. e.g. --table=users")
    private val migration by option("--migration", "-m", help = "Create a migration for the entity").flag()
    override val docUrl = "https://alpas.dev/docs/ozone"

    override fun populateOutputFile(filename: String, actualname: String, vararg parentDirs: String): OutputFile {
        val table = tableName ?: English.plural(filename)
        return OutputFile()
            .target(File(sourceOutputPath("entities", *parentDirs), "${filename.toPascalCase()}.kt"))
            .packageName(makePackageName("entities", *parentDirs))
            .stub(EntityStubs.stub())
            .replacements(
                mapOf(
                    "StubTableClazzName" to table.toPascalCase(),
                    "StubTableName" to table.toSnakeCase().toLowerCase()
                )
            )
    }

    override fun onCompleted(outputFiles: List<OutputFile>) {
        super.onCompleted(outputFiles)
        if (migration) {
            outputFiles.forEach {
                val table = tableName ?: English.plural(it.target.nameWithoutExtension)
                val migrationOptions = mutableListOf("create_${table.toSnakeCase()}_table", "--create=${table}")
                if (quiet) {
                    migrationOptions.add("--quiet")
                }
                MakeMigrationCommand(srcPackage).main(migrationOptions)
            }
        }
    }
}
