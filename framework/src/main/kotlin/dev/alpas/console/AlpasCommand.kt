package dev.alpas.console

class AlpasCommand(private val commandAliases: Map<String, List<String>> = emptyMap()) :
    Command(help = "Webframework for the rest of us", name = "") {

    override fun aliases() = mapOf("migrate" to listOf("db:migrate")) + commandAliases

    fun execute(args: Array<String>) {
        main(args)
    }

    fun execute(arg: String) {
        execute(arrayOf(arg))
    }
}
