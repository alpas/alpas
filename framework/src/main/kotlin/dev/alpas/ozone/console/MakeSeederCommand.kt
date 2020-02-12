package dev.alpas.ozone.console

import dev.alpas.console.GeneratorCommand
import dev.alpas.console.OutputFile
import dev.alpas.extensions.toPascalCase
import dev.alpas.ozone.console.stubs.SeederStubs
import java.io.File

class MakeSeederCommand(srcPackage: String) :
    GeneratorCommand(srcPackage, name = "make:seeder", help = "Make a seeder class") {
    override val docUrl = "https://alpas.dev/docs/seeding"

    override fun populateOutputFile(filename: String, actualname: String, vararg parentDirs: String): OutputFile {
        return OutputFile()
            .target(File(sourceOutputPath("database", "seeds", *parentDirs), "${filename.toPascalCase()}.kt"))
            .packageName(makePackageName("database", "seeds", *parentDirs))
            .stub(SeederStubs.stub())
    }
}
