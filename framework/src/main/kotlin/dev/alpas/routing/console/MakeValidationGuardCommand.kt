package dev.alpas.routing.console

import dev.alpas.console.GeneratorCommand
import dev.alpas.console.OutputFile
import dev.alpas.extensions.toPascalCase
import dev.alpas.routing.console.stubs.Stubs
import java.io.File

class MakeValidationGuardCommand(srcPackage: String) :
    GeneratorCommand(srcPackage, name = "make:validation-guard", help = "Make a new Validation Guard.") {

    override fun populateOutputFile(filename: String, actualname: String, vararg parentDirs: String): OutputFile {
      return OutputFile()
            .target(File(sourceOutputPath("guards", *parentDirs), "${filename.toPascalCase()}.kt"))
            .packageName(makePackageName("guards", *parentDirs))
            .stub(Stubs.validationGuardStub())
    }

    override fun onCompleted(outputFile: OutputFile) {
        withColors {
            echo(green("VALIDATION GUARD CREATED 🙌"))
            echo("${brightGreen(outputFile.target.name)}: ${dim(outputFile.target.path)}")
        }
    }
}
