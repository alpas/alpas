package dev.alpas.routing.console

import com.github.freva.asciitable.AsciiTable
import dev.alpas.asYellow
import dev.alpas.auth.AuthConfig
import dev.alpas.console.Command
import dev.alpas.routing.Router

class RouteInfoCommand(private val srcPackage: String, private val router: Router, private val authConfig: AuthConfig) :
    Command(name = "route:info", help = "Print information about all the registered routes") {
    override fun run() {
        val routes = router.routes.map {
            val authOnly = it.authOnly()
            val authChannel = if (authOnly) {
                (it.authChannel ?: authConfig.defaultAuthChannel).let { channel ->
                    if (channel.isEmpty()) "❗️   " else channel
                }
            } else {
                ""
            }

            val authOnlyEmoji = if (authOnly) "  ✅   $authChannel    " else "❌     "

            val guestOnlyEmoji = if (it.guestOnly()) "✅     " else "❌     "
            arrayOf(
                it.method.toString(),
                it.path,
                it.name,
                it.handler.toString().removePrefix(srcPackage).removePrefix("."),
                authOnlyEmoji,
                guestOnlyEmoji
            )
        }
        echo(
            AsciiTable.getTable(
                arrayOf("Method", "Path", "Name", "Handler", "Auth Only?", "Guest Only?"),
                routes.toTypedArray()
            )
        )
    }
}
