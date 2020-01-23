package dev.alpas.runner

import dev.alpas.Alpas
import dev.alpas.http.HttpCall
import dev.alpas.routing.Controller
import dev.alpas.routing.Router

fun main(args: Array<String>) {
    RunnerApp(args) { routes { appRoutes() } }.ignite()
}

class PageController : Controller() {
    fun index(call: HttpCall) {
        call.reply("hello")
    }
}

fun Router.appRoutes() {
    get("/", PageController::class).middlewareGroup("web")
//    get("/makeauth") {
//        make<AlpasCommand>().execute(arrayOf("make:auth"))
//    }
}

class RunnerApp(args: Array<String>, block: Alpas.() -> Unit = {}) : Alpas(args, block) {
//    Uncomment if you want to run a console command
//    override fun shouldLoadConsoleCommands(): Boolean {
//        return true
//    }
}
