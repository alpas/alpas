package dev.alpas.routing.console

import dev.alpas.console.GeneratorCommand
import dev.alpas.console.OutputFile
import dev.alpas.extensions.toPascalCase
import dev.alpas.routing.console.stubs.Stubs
import java.io.File

class MakeControllerCommand(srcPackage: String) :
    GeneratorCommand(srcPackage, name = "make:controller", help = "Create a new controller class.") {

    override fun populateOutputFile(filename: String, actualname: String, vararg parentDirs: String): OutputFile {
        return OutputFile()
            .target(File(sourceOutputPath("controllers", *parentDirs), "${filename.toPascalCase()}.kt"))
            .packageName(makePackageName("controllers", *parentDirs))
            .stub(Stubs.controllerPlainStub())
    }

    override fun onCompleted(outputFile: OutputFile) {
        withColors {
            echo(green("CONTROLLER CREATED 🙌"))
            echo("${brightGreen(outputFile.target.name)}: ${dim(outputFile.target.path)}")
        }
    }
}
