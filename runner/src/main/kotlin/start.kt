package dev.alpas.runner

import dev.alpas.Alpas
import dev.alpas.http.HttpCall
import dev.alpas.http.HttpCallHook
import dev.alpas.http.RenderContext
import dev.alpas.routing.Controller
import dev.alpas.routing.Route
import dev.alpas.routing.Router

fun main(args: Array<String>) {
    RunnerApp(args) {
        routes { appRoutes() }
        registerCallHook(TestCallHook::class)
    }.ignite()
}

class TestCallHook() : HttpCallHook {
    override fun beforeClose(call: HttpCall, cleanClose: Boolean) {
        println(">>>>>>>>>>>>>>>before close")
    }

    override fun beforeRender(renderContext: RenderContext) {
        println(">>>>>>>>>>>>>>>before render")
    }

    override fun beforeRouteHandle(call: HttpCall, route: Route) {
        println(">>>>>>>>>>>>>>>before route handle")
    }

    override fun boot(call: HttpCall) {
        println(">>>>>>>>>>>>>>>boot")
    }

    override fun register(call: HttpCall) {
        println(">>>>>>>>>>>>>>>register")
    }
}

class PageController : Controller() {
    fun index(call: HttpCall) {
        call.render("home")
//        call.reply("hello")
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
