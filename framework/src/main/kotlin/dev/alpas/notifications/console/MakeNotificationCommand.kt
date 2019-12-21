package dev.alpas.notifications.console

import dev.alpas.console.GeneratorCommand
import dev.alpas.console.OutputFile
import dev.alpas.extensions.toPascalCase
import dev.alpas.notifications.console.stubs.Stubs
import java.io.File

class MakeNotificationCommand(srcPackage: String) :
    GeneratorCommand(srcPackage, name = "make:notification", help = "Create a new mailable notification.") {

    override fun populateOutputFile(filename: String, actualname: String, vararg parentDirs: String): OutputFile {
        return OutputFile()
            .target(File(sourceOutputPath("notifications", *parentDirs), "${filename.toPascalCase()}.kt"))
            .packageName(makePackageName("notifications", *parentDirs))
            .stub(Stubs.notificationStub())
    }

    override fun onCompleted(outputFile: OutputFile) {
        withColors {
            echo(green("MakeNotificationCommand CREATED 🙌"))
            echo("${brightGreen(outputFile.target.name)}: ${dim(outputFile.target.path)}")
        }
    }
}
