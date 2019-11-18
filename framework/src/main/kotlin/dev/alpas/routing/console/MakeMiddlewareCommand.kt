package dev.alpas.routing.console

import dev.alpas.console.GeneratorCommand
import dev.alpas.console.OutputFile
import dev.alpas.extensions.toPascalCase
import dev.alpas.routing.console.stubs.Stubs
import java.io.File

class MakeMiddlewareCommand(srcPackage: String) :
    GeneratorCommand(srcPackage, name = "make:middleware", help = "Create a new middleware class.") {

    override fun populateOutputFile(filename: String, actualname: String, vararg parentDirs: String): OutputFile {
        return OutputFile()
            .target(File(sourceOutputPath("middleware", *parentDirs), "${filename.toPascalCase()}.kt"))
            .packageName(makePackageName("middleware", *parentDirs))
            .stub(Stubs.middlewareStub())
    }

    override fun onCompleted(outputFile: OutputFile) {
        withColors {
            echo(green("MIDDLEWARE CREATED 🙌"))
            echo("${brightGreen(outputFile.target.name)}: ${dim(outputFile.target.path)}")
        }
    }
}
