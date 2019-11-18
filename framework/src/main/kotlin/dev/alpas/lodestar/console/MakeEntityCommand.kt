package dev.alpas.lodestar.console

import com.cesarferreira.pluralize.pluralize
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import dev.alpas.console.GeneratorCommand
import dev.alpas.console.OutputFile
import dev.alpas.extensions.toPascalCase
import dev.alpas.extensions.toSnakeCase
import dev.alpas.lodestar.console.stubs.EntityStubs
import java.io.File

class MakeEntityCommand(srcPackage: String) :
    GeneratorCommand(
        srcPackage,
        name = "make:entity",
        help = "Create an entity class with or without the corresponding table."
    ) {
    // todo: make mututally exclusive
    private val skipTable by option("--no-table", help = "Don't create the corresponding entity table.").flag()
    private val tableName by option("--table", help = "Name of the table.")
    private val simple by option("--simple", help = "Create a simple data based entity.").flag()

    override fun populateOutputFile(filename: String, actualname: String, vararg parentDirs: String): OutputFile {
        val table = tableName ?: filename.pluralize()
        return OutputFile()
            .target(File(sourceOutputPath("entities", *parentDirs), "${filename.toPascalCase()}.kt"))
            .packageName(makePackageName("entities", *parentDirs))
            .stub(entityStub())
            .replacements(
                mapOf(
                    "StubTableClazzName" to table.toPascalCase(),
                    "StubTableName" to table.toSnakeCase().toLowerCase()
                )
            )
    }

    private fun entityStub(): String {
        return if (simple) EntityStubs.simpleStub(!skipTable) else EntityStubs.stub(!skipTable)
    }

    override fun onCompleted(outputFile: OutputFile) {
        withColors {
            echo(green("ENTITY CREATED 🙌"))
            echo("${brightGreen(outputFile.target.name)}: ${dim(outputFile.target.path)}")
        }
    }
}
