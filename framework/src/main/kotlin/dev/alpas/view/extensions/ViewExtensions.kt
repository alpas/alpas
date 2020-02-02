package dev.alpas.view.extensions

import com.mitchellbosecke.pebble.extension.AbstractExtension
import com.mitchellbosecke.pebble.extension.Filter
import com.mitchellbosecke.pebble.extension.Function
import com.mitchellbosecke.pebble.tokenParser.TokenParser
import dev.alpas.Application
import dev.alpas.config
import dev.alpas.make
import dev.alpas.view.ViewConfig
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties

internal class BuiltInExtensions(private val app: Application) : AbstractExtension() {
    override fun getFunctions(): Map<String, Function> {
        return mapOf(
            "mix" to MixFunction(app.make()),
            "route" to RouteFunction(),
            "hasRoute" to HasRouteFunction(),
            "routeIs" to RouteIsFunction(),
            "routeIsOneOf" to RouteIsOneOfFunction(),
            "old" to OldFunction(),
            "config" to ConfigFunction(),
            "env" to EnvFunction(),
            "errors" to ErrorsFunction(),
            "hasError" to HasErrorFunction(),
            "whenError" to WhenErrorFunction(),
            "firstError" to FirstErrorFunction(),
            "flash" to FlashFunction(),
            "hasFlash" to HasFlashFunction()
        )
    }

    override fun getFilters(): Map<String, Filter> {
        return mapOf(
            "int" to IntFilter(),
            "json_encode" to JsonEncodeFilter(),
            "ago" to AgoFilter()
        )
    }

    override fun getTokenParsers(): List<TokenParser> {
        return mutableListOf(
            CsrfTokenParser(),
            ConditionalTokenParser("auth") { it.call.isAuthenticated },
            ConditionalTokenParser("guest") { !it.call.isAuthenticated }
        )
    }

    override fun getGlobalVariables(): Map<String, Any> {
        val envEntries: Map<String, String> = app.env.entries
        return mapOf("_configs" to makeViewConfigs(app), "_env" to envEntries)
    }

    @Suppress("UNCHECKED_CAST")
    private fun makeViewConfigs(app: Application): Map<String, Any?> {
        val configs = app.configs.flatMap { config ->
            config::class.memberProperties.map { property ->
                val name = property.name
                val value = readInstanceProperty(config, property as KProperty1<Any, *>)
                "${config::class.simpleName?.replace("Config", "")?.toLowerCase()}.$name" to value
            }
        }.toMap()

        return app.config<ViewConfig>().configsAvailableForView() + configs
    }

    private fun readInstanceProperty(instance: Any, property: KProperty1<Any, *>): Any? {
        return if (property.visibility == KVisibility.PUBLIC) {
            property.get(instance)
        } else {
            null
        }
    }
}
