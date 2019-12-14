package dev.alpas.view

import com.mitchellbosecke.pebble.extension.AbstractExtension
import dev.alpas.Application
import dev.alpas.ServiceProvider
import dev.alpas.config
import dev.alpas.http.HttpCall
import dev.alpas.make
import dev.alpas.view.extensions.BuiltInExtensions
import dev.alpas.view.extensions.ConditionalTokenParser
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties

@Suppress("unused")
open class ViewServiceProvider : ServiceProvider {
    private var isEnabled = false
    override fun register(app: Application) {
        isEnabled = app.config { ViewConfig(app.env) }.isEnabled
        if (isEnabled) {
            app.singleton(Mix(app))
            app.singleton(PebbleViewRenderer(app))
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun boot(app: Application) {
        if (!isEnabled) {
            return
        }

        app.make<ViewRenderer>().also {
            val registrar = ConditionalTagsRegistrar()
            registerConditionalTags(registrar)

            it.extend(BuiltInExtensions(app, registrar.tags))
            loadExtensions(app).forEach { ext ->
                it.extend(ext)
            }
            val envEntries = app.env.entries.map { entry -> Pair(entry.key, entry.value) }.toMap()
            it.addConfigs("_configs", makeViewConfigs(app))
            it.addConfigs("_env", envEntries)
            it.boot()
        }
    }

    protected open fun loadExtensions(app: Application): Iterable<AbstractExtension> {
        return emptyList()
    }

    protected open fun registerConditionalTags(registrar: ConditionalTagsRegistrar) {
    }

    @Suppress("UNCHECKED_CAST")
    private fun makeViewConfigs(app: Application): Map<String, Any?> {
        val configs = app.configs.flatMap { config ->
            config::class.memberProperties.map { property ->
                val name = property.name
                val value = readInstanceProperty(config, property as KProperty1<Any, *>)
                Pair("${config::class.simpleName?.replace("Config", "")?.toLowerCase()}.$name", value)
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

class ConditionalTagsRegistrar(val tags: MutableList<ConditionalTokenParser> = mutableListOf()) {
    fun register(tagName: String, condition: (call: HttpCall) -> Boolean) {
        tags.add(ConditionalTokenParser(tagName, condition))
    }
}
