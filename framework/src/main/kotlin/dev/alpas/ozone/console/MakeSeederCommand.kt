package dev.alpas.ozone.console

import dev.alpas.console.GeneratorCommand
import dev.alpas.console.OutputFile
import dev.alpas.extensions.toPascalCase
import dev.alpas.ozone.console.stubs.SeederStubs
import java.io.File

class MakeSeederCommand(srcPackage: String) :
    GeneratorCommand(srcPackage, name = "make:seeder", help = "Make a seeder class") {

    override fun populateOutputFile(filename: String, actualname: String, vararg parentDirs: String): OutputFile {
        return OutputFile()
            .target(File(sourceOutputPath("database", "seeds", *parentDirs), "${filename.toPascalCase()}.kt"))
            .packageName(makePackageName("database", "seeds", *parentDirs))
            .stub(SeederStubs.stub())
    }

    override fun onCompleted(outputFile: OutputFile) {
        withColors {
            echo(green("SEEDER CREATED 🙌"))
            echo("${brightGreen(outputFile.target.name)}: ${dim(outputFile.target.path)}")
        }
    }
}
