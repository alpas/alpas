package dev.alpas.console

import dev.alpas.console.stubs.Stubs
import dev.alpas.extensions.toPascalCase
import java.io.File

class MakeServiceProviderCommand(srcPackage: String) :
    GeneratorCommand(srcPackage, name = "make:provider", help = "Create a new service provider class") {
    override val docUrl = "https://alpas.dev/docs/serivce-providers"

    override fun populateOutputFile(filename: String, actualname: String, vararg parentDirs: String): OutputFile {
        return OutputFile()
            .target(File(sourceOutputPath("providers", *parentDirs), "${filename.toPascalCase()}.kt"))
            .packageName(makePackageName("providers", *parentDirs))
            .stub(Stubs.serviceProviderStub())
    }
}
