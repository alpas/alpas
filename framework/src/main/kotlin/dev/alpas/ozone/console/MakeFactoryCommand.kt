package dev.alpas.ozone.console

import com.cesarferreira.pluralize.pluralize
import dev.alpas.console.GeneratorCommand
import dev.alpas.console.OutputFile
import dev.alpas.extensions.toPascalCase
import dev.alpas.ozone.console.stubs.FactoryStubs
import java.io.File

private const val SUFFIX = "factory"

class MakeFactoryCommand(srcPackage: String) :
    GeneratorCommand(srcPackage, name = "make:factory", help = "Make an entity factory class") {

    override fun populateOutputFile(filename: String, actualname: String, vararg parentDirs: String): OutputFile {
        // append SUFFIX to the filename if it doesn't end with the SUFFIX OR the filename itself is not called SUFFIX
        val outputFilename =
            if (filename.toLowerCase() != SUFFIX && filename.toLowerCase().endsWith(SUFFIX)) filename else "${filename}${SUFFIX.toPascalCase()}"
        val entitiesPackage = makePackageName("entities")
        val entityName =
            if (actualname.toLowerCase() != SUFFIX) actualname.removeSuffix(SUFFIX.toPascalCase()) else actualname.toPascalCase()
        return OutputFile()
            .target(File(sourceOutputPath("database", "factories", *parentDirs), "${outputFilename.toPascalCase()}.kt"))
            .packageName(makePackageName("database", "factories", *parentDirs))
            .stub(FactoryStubs.stub())
            .replacements(
                "StubEntityClazzName" to entityName,
                "StubTableClazzName" to entityName.pluralize(),
                "StubEntitiesPackage" to entitiesPackage
            )
    }

    override fun onCompleted(outputFile: OutputFile) {
        withColors {
            echo(green("FACTORY CREATED 🙌"))
            echo("${brightGreen(outputFile.target.name)}: ${dim(outputFile.target.path)}")
        }
    }
}
