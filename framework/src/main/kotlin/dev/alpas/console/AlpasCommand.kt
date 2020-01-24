package dev.alpas.console

import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.output.CliktHelpFormatter
import com.github.ajalt.clikt.output.HelpFormatter
import com.github.ajalt.mordant.TermColors

class AlpasCommand(private val commandAliases: Map<String, List<String>> = emptyMap()) :
    Command(
        help = "The Rapid and Delightful Kotlin Web Framework.", name = "",
        autoCompleteEnvvar = "ALPAS_AUTOCOMPLETE_SHELL"
    ) {

    override fun aliases() =
        mapOf(
            "migrate" to listOf("db:migrate"),
            "make:rule" to listOf("make:validation-rule"),
            "make:guard" to listOf("make:validation-guard"),
            "make:key" to listOf("key:generate")
        ) + commandAliases

    fun execute(args: Array<String>) {
        main(args)
    }

    fun execute(arg: String) {
        execute(arrayOf(arg))
    }

    init {
        context { helpFormatter = ColorHelpFormatter }
    }
}

private object ColorHelpFormatter : CliktHelpFormatter() {
    private val tc = TermColors(TermColors.Level.ANSI16)

    override fun renderTag(tag: String, value: String) = tc.green(super.renderTag(tag, value))
    override fun renderOptionName(name: String) = tc.green(super.renderOptionName(name))
    override fun renderArgumentName(name: String) = tc.green(super.renderArgumentName(name))
    override fun renderSubcommandName(name: String) = tc.yellow(super.renderSubcommandName(name))
    override fun renderSectionTitle(title: String) = (tc.bold + tc.yellow)(super.renderSectionTitle(title))
    override fun optionMetavar(option: HelpFormatter.ParameterHelp.Option) = tc.green(super.optionMetavar(option))
}
