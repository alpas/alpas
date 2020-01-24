package dev.alpas.routing.console

import com.github.freva.asciitable.AsciiTable
import dev.alpas.auth.AuthConfig
import dev.alpas.console.Command
import dev.alpas.routing.Router

class RouteListCommand(private val router: Router, private val authConfig: AuthConfig) :
    Command(name = "route:list", help = "List all the registered routes") {
    override fun run() {
        val routes = router.routes.map {
            val authOnly = it.authOnly()
            val authOnlyEmoji = if (authOnly) "✅     " else "❌     "
            val authChannel = if (authOnly) {
                (it.authChannel ?: authConfig.defaultAuthChannel).let { channel ->
                    if (channel.isEmpty()) "❗️   " else channel
                }
            } else {
                ""
            }

            val guestOnlyEmoji = if (it.guestOnly()) "✅     " else "❌     "
            arrayOf(
                it.method.toString(),
                it.path,
                it.name,
                it.handler.toString(),
                authOnlyEmoji,
                authChannel,
                guestOnlyEmoji
            )
        }
        println(
            AsciiTable.getTable(
                arrayOf("Method", "Path", "Name", "Handler", "Auth Only?", "Auth Channel", "Guest Only?"),
                routes.toTypedArray()
            )
        )
    }
}
