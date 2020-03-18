package dev.alpas

import java.net.URI

interface AppHook {
    fun onAppStarted(app: Application, uri: URI) {}
    fun onAppStopped(){}
}
