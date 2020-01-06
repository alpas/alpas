package dev.alpas.runner

import dev.alpas.Alpas
import dev.alpas.http.HttpCall
import dev.alpas.make
import dev.alpas.routing.Controller
import dev.alpas.routing.Router

fun main(args: Array<String>) {
    Alpas(args) {
        make<Router>() {
            get("/", PageController::class)
        }
    }.ignite()
}

class PageController : Controller() {
    fun index(call: HttpCall) {
        call.reply("hello")
    }
}
