package dev.alpas.view.extensions

import com.mitchellbosecke.pebble.extension.Filter
import com.mitchellbosecke.pebble.extension.Function
import dev.alpas.Application
import dev.alpas.config
import dev.alpas.make
import dev.alpas.session.CSRF_SESSION_KEY
import dev.alpas.view.ConditionalTags
import dev.alpas.view.CustomFunctions
import dev.alpas.view.CustomTags
import dev.alpas.view.ViewConfig
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties

internal class PebbleExtensions : PebbleExtension {
    override fun register(app: Application, conditionalTags: ConditionalTags) {
        conditionalTags.add("auth") {
            call.isAuthenticated
        }
        conditionalTags.add("guest") {
            !call.isAuthenticated
        }
    }

    override fun register(app: Application, customTags: CustomTags) {
        customTags.add("csrf") {
            val csrf = this.context.getVariable(CSRF_SESSION_KEY)
            """<input type="hidden" name="$CSRF_SESSION_KEY" value="$csrf">"""
        }
    }

    override fun register(app: Application, customFunctions: CustomFunctions) {
        customFunctions.add("spoof", listOf("method")) {
            val method = args?.get("method")
                ?: throw Exception("spoof() function requires the name of the method to spoof - either PUT, PATCh, or DELETE. Called from (${this.templateName} line no. $lineNumber)")
            """<input type="hidden" name="_method" value="${method.toString().toLowerCase()}">"""
        }
    }

    override fun functions(app: Application): Map<String, Function> {
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

    override fun filters(app: Application): Map<String, Filter> {
        return mapOf(
            "int" to IntFilter(),
            "json_encode" to JsonEncodeFilter(),
            "ago" to AgoFilter()
        )
    }

    override fun globalVariables(app: Application): Map<String, Any> {
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
