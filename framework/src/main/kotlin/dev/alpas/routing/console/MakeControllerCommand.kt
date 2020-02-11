package dev.alpas.routing.console

import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import dev.alpas.console.GeneratorCommand
import dev.alpas.console.OutputFile
import dev.alpas.extensions.toPascalCase
import dev.alpas.relativize
import dev.alpas.routing.console.stubs.Stubs
import java.io.File

class MakeControllerCommand(srcPackage: String) :
    GeneratorCommand(srcPackage, name = "make:controller", help = "Create a new controller class") {

    private val isResourceful by option("--resource", "-r", help = "Create a resourceful controller").flag()

    override fun populateOutputFile(filename: String, actualname: String, vararg parentDirs: String): OutputFile {
        return OutputFile()
            .target(File(sourceOutputPath("controllers", *parentDirs), "${filename.toPascalCase()}.kt"))
            .packageName(makePackageName("controllers", *parentDirs))
            .stub(Stubs.controllerStub(isResourceful))
    }

    override fun onCompleted(outputFiles: List<OutputFile>) {
        withColors {
            if (isResourceful) {
                outputFiles.forEach {
                    echo("")
                    val path = sourceOutputPath()?.relativize(it.target.path)
                    echo("${green(" ✓")} ${brightGreen(path!!)}")
                    echo(
                        "${yellow(" Don't forget to add")} ${blue(
                            "resources<${it.target.nameWithoutExtension}>()"
                        )} ${yellow("to your routes.")}"
                    )
                }
            } else {
                echo("")
                outputFiles.forEach {
                    val path = sourceOutputPath()?.relativize(it.target.path)
                    echo("${green(" ✓")} ${brightGreen(path!!)}")
                }
            }
            echo("")
            echo(yellow("https://alpas.dev/docs/controllers"))
        }
    }
}
