package dev.alpas.view.extensions

import com.mitchellbosecke.pebble.attributes.AttributeResolver
import com.mitchellbosecke.pebble.extension.*
import com.mitchellbosecke.pebble.extension.Function
import com.mitchellbosecke.pebble.operator.BinaryOperator
import com.mitchellbosecke.pebble.operator.UnaryOperator
import com.mitchellbosecke.pebble.tokenParser.TokenParser
import dev.alpas.Application

internal class PebbleExtensionWrapper(private val pebbleExtension: PebbleExtension, private val app: Application) :
    AbstractExtension() {
    override fun getAttributeResolver(): List<AttributeResolver>? {
        return pebbleExtension.attributeResolver(app)
    }

    override fun getBinaryOperators(): List<BinaryOperator>? {
        return pebbleExtension.binaryOperators(app)
    }

    override fun getFilters(): Map<String, Filter>? {
        return pebbleExtension.filters(app)
    }

    override fun getFunctions(): Map<String, Function>? {
        return pebbleExtension.functions(app)
    }

    override fun getGlobalVariables(): Map<String, Any>? {
        return pebbleExtension.globalVariables(app)
    }

    override fun getNodeVisitors(): List<NodeVisitorFactory>? {
        return pebbleExtension.nodeVisitors(app)
    }

    override fun getTokenParsers(): List<TokenParser>? {
        return pebbleExtension.tokenParsers(app)
    }

    override fun getTests(): Map<String, Test>? {
        return pebbleExtension.tests(app)
    }

    override fun getUnaryOperators(): List<UnaryOperator>? {
        return pebbleExtension.unaryOperators(app)
    }
}
