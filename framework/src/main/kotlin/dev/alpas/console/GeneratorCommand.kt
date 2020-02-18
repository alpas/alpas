package dev.alpas.console

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import dev.alpas.relativize
import java.nio.file.Paths

abstract class GeneratorCommand(
    protected val srcPackage: String,
    help: String = "",
    epilog: String = "",
    name: String? = null,
    invokeWithoutSubcommand: Boolean = false,
    printHelpOnEmptyArgs: Boolean = false,
    helpTags: Map<String, String> = emptyMap(),
    autoCompleteEnvvar: String? = "",
    private val allowMultipleNames: Boolean = true
) : Command(help, epilog, name, invokeWithoutSubcommand, printHelpOnEmptyArgs, helpTags, autoCompleteEnvvar) {
    protected open val force by option("--force", "-f", help = "Force generate file(s)").flag()
    protected open val names by argument().multiple()
    protected open val docUrl: String? = null

    override fun run() {
        if (!allowMultipleNames && names.size > 1) {
            error("$commandName supports one name")
            showHelp()
            return
        }
        if (names.isEmpty()) {
            error("$commandName needs at least one name")
            showHelp()
            return
        }
        prepareOutputFiles()
    }

    protected open fun outputFilenames(): List<String> {
        return names;
    }

    private fun prepareOutputFiles() {
        val outputFiles = outputFilenames().map { name ->
            val pathComponents = name.split("/")
            val parentDirs = pathComponents.subList(0, pathComponents.count() - 1).map { dir ->
                dir.toLowerCase()
            }.toTypedArray()

            val filename = pathComponents.last()
            populateOutputFile(filename, name, *parentDirs).also { outputFile ->
                // check if any of the files already exist, if so return without creating any other file
                if (!force && outputFile.exists()) {
                    error("${outputFile.target.path} already exists.")
                    info("To override files, pass --force flag.")
                    return
                }
            }
        }

        outputFiles.forEach { outputFile ->
            try {
                outputFile.dump()
            } catch (e: Exception) {
                e.message?.let { error(it) }
            }
        }
        onCompleted(outputFiles)
    }

    protected fun sourceOutputPath(vararg dirs: String): String? {
        return Paths.get(srcDir, *dirs).toAbsolutePath().toString()
    }

    protected fun templatesOutputPath(vararg dirs: String): String? {
        return Paths.get(templatesDir.path, *dirs).toAbsolutePath().toString()
    }

    protected fun makePackageName(vararg dirs: String): String {
        return listOf(srcPackage, *dirs).joinToString(".")
    }

    protected open fun onCompleted(outputFiles: List<OutputFile>) {
        withColors {
            outputFiles.forEach {
                println()
                val path = sourceOutputPath()?.relativize(it.target.path)
                echo("${green(" ✓")} ${brightGreen(path!!)}")
                docUrl?.let { url ->
                    println()
                    echo(yellow(url))
                }
            }
        }
    }

    abstract fun populateOutputFile(filename: String, actualname: String, vararg parentDirs: String): OutputFile
}
