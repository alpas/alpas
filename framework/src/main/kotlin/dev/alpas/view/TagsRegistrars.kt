package dev.alpas.view

import com.mitchellbosecke.pebble.extension.AbstractExtension
import com.mitchellbosecke.pebble.extension.Function
import dev.alpas.Application
import dev.alpas.make
import dev.alpas.view.extensions.ConditionalTokenParser
import dev.alpas.view.extensions.CustomTokenParser

interface CustomTags {
    fun add(tagName: String, callback: TagContext.() -> String)
}

internal class CustomTagsImpl(private val viewRenderer: ViewRenderer) : CustomTags {
    override fun add(tagName: String, callback: TagContext.() -> String) {
        viewRenderer.extend(object : AbstractExtension() {
            override fun getTokenParsers() = listOf(CustomTokenParser(tagName, callback, viewRenderer))
        })
    }
}

interface CustomFunctions {
    fun add(functionName: String, argNames: List<String>? = null, callback: FunctionContext.() -> String)
}

class CustomFunctionsImpl(private val viewRenderer: ViewRenderer) : CustomFunctions {
    override fun add(functionName: String, argNames: List<String>?, callback: FunctionContext.() -> String) {
        viewRenderer.extend(object : AbstractExtension() {
            override fun getFunctions(): Map<String, Function> {
                return mapOf(functionName to CustomFunction(argNames, callback, viewRenderer))
            }
        })
    }
}

interface ConditionalTags {
    fun add(tagName: String, condition: TagContext.() -> Boolean)
}

internal class ConditionalTagsImpl(private val viewRenderer: ViewRenderer) : ConditionalTags {
    override fun add(tagName: String, condition: (context: TagContext) -> Boolean) {
        viewRenderer.extend(object : AbstractExtension() {
            override fun getTokenParsers() = listOf(ConditionalTokenParser(tagName, condition))
        })
    }
}

fun Application.addCustomTag(name: String, callback: TagContext.() -> String) {
    make<CustomTags>().add(name, callback)
}

fun Application.addConditionalTag(name: String, callback: TagContext.() -> Boolean) {
    make<ConditionalTags>().add(name, callback)
}

fun Application.addCustomFunction(
    name: String,
    argNames: List<String>? = null,
    callback: FunctionContext.() -> String
) {
    make<CustomFunctions>().add(name, argNames, callback)
}
