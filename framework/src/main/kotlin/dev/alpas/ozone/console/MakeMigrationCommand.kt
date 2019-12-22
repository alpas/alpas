package dev.alpas.ozone.console

import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.groups.required
import com.github.ajalt.clikt.parameters.groups.single
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import dev.alpas.console.GeneratorCommand
import dev.alpas.console.OutputFile
import dev.alpas.extensions.toPascalCase
import dev.alpas.ozone.console.stubs.MigrationStubs
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private sealed class MigrationTable(val name: String)
private class ModifyTable(name: String) : MigrationTable(name)
private class CreateTable(name: String) : MigrationTable(name)

class MakeMigrationCommand(srcPackage: String) :
    GeneratorCommand(srcPackage, name = "make:migration", help = "Create a new migration class.") {
    private val action by mutuallyExclusiveOptions<MigrationTable>(
        option("--create", help = "The name of the entity table to create. e.g. --create=Users")
            .convert { CreateTable(it) },
        option("--modify", help = "The name of the entity table to modify. e.g. --modify=Users")
            .convert { ModifyTable(it) }
    ).single().required()


    override fun populateOutputFile(filename: String, actualname: String, vararg parentDirs: String): OutputFile {
        val packageName = makePackageName("database", "migrations", *parentDirs)
        val entityPackageName = makePackageName("entities", *parentDirs)
        val outputPath = sourceOutputPath("database", "migrations", *parentDirs)
        val datePrefix = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("y_MM_dd_Hmmss"))

        return OutputFile()
            .target(File(outputPath, "${datePrefix}_$filename.kt"))
            .packageName(packageName)
            .className(filename)
            .replacements(
                mapOf(
                    "StubTableName" to action.name.toPascalCase(),
                    "StubEntityPackageName" to entityPackageName
                )
            )
            .stub(stubFor(action))
    }

    override fun onCompleted(outputFile: OutputFile) {
        withColors {
            echo(green("MIGRATION CREATED 🙌"))
            echo("${brightGreen(outputFile.target.name)}: ${dim(outputFile.target.path)}")
        }
    }

    private fun stubFor(action: MigrationTable): String {
        return when (action) {
            is CreateTable -> MigrationStubs.createTableMigrationStub()
            else -> MigrationStubs.modifyTableMigrationStub()
        }
    }
}
