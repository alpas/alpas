package dev.alpas.queue.console

import dev.alpas.console.GeneratorCommand
import dev.alpas.console.OutputFile
import dev.alpas.extensions.toPascalCase
import dev.alpas.queue.console.stubs.Stubs
import java.io.File

class MakeJobCommand(srcPackage: String) : GeneratorCommand(srcPackage, name = "make:job", help = "Create a new job.") {
    override fun populateOutputFile(filename: String, actualname: String, vararg parentDirs: String): OutputFile {
        return OutputFile()
            .target(File(sourceOutputPath("jobs", *parentDirs), "${filename.toPascalCase()}.kt"))
            .packageName(makePackageName("jobs", *parentDirs))
            .stub(Stubs.jobStub())
    }

    override fun onCompleted(outputFile: OutputFile) {
        withColors {
            echo(green("JOB CREATED 🙌"))
            echo("${brightGreen(outputFile.target.name)}: ${dim(outputFile.target.path)}")
        }
    }
}

