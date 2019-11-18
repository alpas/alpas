package dev.alpas.http

import org.eclipse.jetty.util.log.Logger

internal class NoLogging : Logger {
    companion object {
        fun applyToJetty() {
            org.eclipse.jetty.util.log.Log.setLog(NoLogging())
        }
    }

    override fun getName() = "no"
    override fun warn(msg: String, vararg args: Any) {}
    override fun warn(thrown: Throwable) {}
    override fun warn(msg: String, thrown: Throwable) {}
    override fun info(msg: String, vararg args: Any) {}
    override fun info(thrown: Throwable) {}
    override fun info(msg: String, thrown: Throwable) {}
    override fun isDebugEnabled() = false
    override fun setDebugEnabled(enabled: Boolean) {}
    override fun debug(msg: String?, value: Long) {}
    override fun debug(msg: String, vararg args: Any) {}
    override fun debug(thrown: Throwable) {}
    override fun debug(msg: String, thrown: Throwable) {}
    override fun getLogger(name: String) = this
    override fun ignore(ignored: Throwable) {}
}
