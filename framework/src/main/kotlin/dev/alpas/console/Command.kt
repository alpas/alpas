package dev.alpas.console

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.TermColors
import dev.alpas.*
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

abstract class Command(
    help: String = "",
    epilog: String = "",
    name: String? = null,
    invokeWithoutSubcommand: Boolean = false,
    printHelpOnEmptyArgs: Boolean = false,
    helpTags: Map<String, String> = emptyMap(),
    autoCompleteEnvvar: String? = ""
) : CliktCommand(
    help = help,
    epilog = epilog,
    name = name,
    invokeWithoutSubcommand = invokeWithoutSubcommand,
    printHelpOnEmptyArgs = printHelpOnEmptyArgs,
    helpTags = helpTags,
    autoCompleteEnvvar = autoCompleteEnvvar
) {
    internal val rootDir: String by lazy {
        System.getenv(ROOT_DIR_KEY) ?: System.getProperty(ROOT_DIR_KEY)
        ?: throw Exception("Root directory must be defined when running from a console.")
    }
    protected val srcDir: String by lazy {
        System.getenv(SRC_DIR_KEY) ?: System.getProperty(SRC_DIR_KEY)
        ?: throw Exception("Source directory must be defined when running from a console.")
    }
    val resourcesDir by lazy { File(Paths.get(File(srcDir).parentFile.absolutePath, "resources").toUri()) }
    val templatesDir by lazy { File(resourcesDir, "templates") }
    protected val quiet by option("--quiet", "-q", help = "Don't print any non-error messages").flag()

    override fun run() = Unit

    internal open fun addCommands(commands: Iterable<Command>) {
        subcommands(commands)
    }

    internal open fun addCommands(command: Command, vararg commands: Command) {
        subcommands(command, *commands)
    }

    protected fun error(msg: String) {
        msg.printAsError()
    }

    protected fun info(msg: String) {
        if (!quiet) {
            msg.printAsInfo()
        }
    }

    protected fun success(msg: String) {
        if (!quiet) {
            msg.printAsSuccess()
        }
    }

    protected fun warning(msg: String) {
        if (!quiet) {
            msg.printAsWarning()
        }
    }

    fun showHelp() {
        println(getFormattedHelp())
    }

    fun withColors(output: TermColors.() -> Unit) {
        if (!quiet) {
            with(terminalColors) {
                output()
            }
        }
    }

    fun relativeToSrc(path: Path): String {
        return srcDir.relativize(path)
    }

    fun relativeToSrc(path: String): String {
        return srcDir.relativize(path)
    }
}

