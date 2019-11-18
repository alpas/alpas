package dev.alpas.view.extensions

import com.mitchellbosecke.pebble.extension.Filter
import com.mitchellbosecke.pebble.extension.Function
import com.mitchellbosecke.pebble.tokenParser.TokenParser
import dev.alpas.Container
import dev.alpas.make
import dev.alpas.view.ViewExtension

internal class BuiltInExtensions(private val container: Container) : ViewExtension() {
    override fun getFunctions(): Map<String, Function> {
        return mapOf(
            "mix" to MixFunction(container.make()),
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
        return listOf(
            CsrfTokenParser(),
            AuthTokenParser("auth"),
            AuthTokenParser("guest")
        )
    }
}
