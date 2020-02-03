package dev.alpas.view.extensions

import com.mitchellbosecke.pebble.attributes.AttributeResolver
import com.mitchellbosecke.pebble.extension.Filter
import com.mitchellbosecke.pebble.extension.Function
import com.mitchellbosecke.pebble.extension.NodeVisitorFactory
import com.mitchellbosecke.pebble.extension.Test
import com.mitchellbosecke.pebble.operator.BinaryOperator
import com.mitchellbosecke.pebble.operator.UnaryOperator
import com.mitchellbosecke.pebble.tokenParser.TokenParser
import dev.alpas.Application
import dev.alpas.view.ConditionalTags
import dev.alpas.view.CustomTags

interface PebbleExtension {
    fun register(app: Application, conditionalTags: ConditionalTags, customTags: CustomTags) {}

    fun boot(app: Application) {}

    fun attributeResolver(app: Application): List<AttributeResolver>? {
        return null
    }

    fun tokenParsers(app: Application): List<TokenParser>? {
        return null
    }

    fun unaryOperators(app: Application): List<UnaryOperator>? {
        return null
    }

    fun tests(app: Application): Map<String, Test>? {
        return null
    }

    fun nodeVisitors(app: Application): List<NodeVisitorFactory>? {
        return null
    }

    fun globalVariables(app: Application): Map<String, Any>? {
        return null
    }

    fun functions(app: Application): Map<String, Function>? {
        return null
    }

    fun filters(app: Application): Map<String, Filter>? {
        return null
    }

    fun binaryOperators(app: Application): List<BinaryOperator>? {
        return null
    }
}
