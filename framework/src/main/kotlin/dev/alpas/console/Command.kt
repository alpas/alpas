package dev.alpas.console

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.mordant.TermColors
import dev.alpas.ROOT_DIR_KEY
import dev.alpas.SRC_DIR_KEY
import dev.alpas.printAsError
import dev.alpas.printAsInfo
import dev.alpas.printAsSuccess
import dev.alpas.printAsWarning
import java.io.File
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
        ?: throw Exception("Root directory must be defined when running from console.")
    }
    protected val srcDir: String by lazy {
        System.getenv(SRC_DIR_KEY) ?: System.getProperty(SRC_DIR_KEY)
        ?: throw Exception("Source directory must be defined when running from console.")
    }
    protected val resourcesDir by lazy { File(Paths.get(File(srcDir).parentFile.absolutePath, "resources").toUri()) }
    protected val templatesDir by lazy { File(resourcesDir, "templates") }

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
        msg.printAsInfo()
    }

    protected fun success(msg: String) {
        msg.printAsSuccess()
    }

    protected fun warning(msg: String) {
        msg.printAsWarning()
    }

    fun showHelp() {
        println(getFormattedHelp())
    }

    fun withColors(output: TermColors.() -> Unit) {
        with(TermColors()) {
            output()
        }
    }
}

