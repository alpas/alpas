package dev.alpas.console

import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import dev.alpas.console.stubs.Stubs
import dev.alpas.extensions.toPascalCase
import java.io.File

class MakeCommandCommand(srcPackage: String) :
    GeneratorCommand(srcPackage, name = "make:command", help = "Create a new command.") {
    private val isGenerator by option("--generator", "-g", help = "Create a generator command.").flag()

    override fun populateOutputFile(filename: String, actualname: String, vararg parentDirs: String): OutputFile {
        return OutputFile()
            .target(File(sourceOutputPath("console", "commands", *parentDirs), "${filename.toPascalCase()}.kt"))
            .packageName(makePackageName("console", "commands", *parentDirs))
            .stub(if (isGenerator) Stubs.generatorStub() else Stubs.planStub())
    }

    override fun onCompleted(outputFile: OutputFile) {
        withColors {
            echo(green("COMMAND CREATED 🙌"))
            echo("${brightGreen(outputFile.target.name)}: ${dim(outputFile.target.path)}")
            echo(yellow("Don't forget to register your command."))
        }
    }
}
