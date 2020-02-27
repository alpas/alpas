package dev.alpas.http

import dev.alpas.routing.Route

interface HttpCallHook {
    fun register(call: HttpCall) {}
    fun boot(call: HttpCall) {}
    fun beforeRouteHandle(call: HttpCall, route: Route){}
    fun beforeClose(call: HttpCall, cleanClose: Boolean){}
    fun beforeRender(renderContext: RenderContext){}
    fun beforeErrorRender(renderContext: RenderContext){}
}
