package dev.alpas.routing.console

import dev.alpas.console.GeneratorCommand
import dev.alpas.console.OutputFile
import dev.alpas.extensions.toCamelCase
import dev.alpas.extensions.toPascalCase
import dev.alpas.routing.console.stubs.Stubs
import java.io.File

class MakeValidationGuardCommand(srcPackage: String) :
    GeneratorCommand(srcPackage, name = "make:validation-guard", help = "Make a new validation guard") {
    override val docUrl = "https://alpas.dev/docs/validation"

    override fun populateOutputFile(filename: String, actualname: String, vararg parentDirs: String): OutputFile {
        val outputDir = "guards"
        return OutputFile()
            .target(File(sourceOutputPath(outputDir, *parentDirs), "${filename.toPascalCase()}.kt"))
            .packageName(makePackageName(outputDir, *parentDirs))
            .stub(Stubs.validationGuardStub())
    }
}

class MakeValidationRuleCommand(srcPackage: String) :
    GeneratorCommand(srcPackage, name = "make:validation-rule", help = "Make a new validation rule") {
    override val docUrl = "https://alpas.dev/docs/validation"

    override fun populateOutputFile(filename: String, actualname: String, vararg parentDirs: String): OutputFile {
        val outputDir = "rules"
        return OutputFile()
            .target(File(sourceOutputPath(outputDir, *parentDirs), "${filename.toPascalCase()}.kt"))
            .packageName(makePackageName(outputDir, *parentDirs))
            .replacements(mapOf("StubFunctionName" to actualname.toCamelCase()))
            .stub(Stubs.validationRuleStub())
    }
}
