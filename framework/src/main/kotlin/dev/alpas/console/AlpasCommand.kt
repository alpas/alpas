package dev.alpas.console

class AlpasCommand(private val commandAliases: Map<String, List<String>> = emptyMap()) :
    Command(
        help = "🚀 Rapid web development with Kotlin. Easy, elegant, and productive.", name = "",
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
}
