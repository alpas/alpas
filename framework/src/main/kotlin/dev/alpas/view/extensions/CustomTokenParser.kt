package dev.alpas.view.extensions

import com.mitchellbosecke.pebble.extension.NodeVisitor
import com.mitchellbosecke.pebble.lexer.Token
import com.mitchellbosecke.pebble.node.AbstractRenderableNode
import com.mitchellbosecke.pebble.node.RenderableNode
import com.mitchellbosecke.pebble.parser.Parser
import com.mitchellbosecke.pebble.template.EvaluationContextImpl
import com.mitchellbosecke.pebble.template.PebbleTemplateImpl
import com.mitchellbosecke.pebble.tokenParser.TokenParser
import dev.alpas.validation.SharedDataBag
import dev.alpas.view.TagContext
import dev.alpas.view.ViewRenderer
import dev.alpas.view.httpCall
import java.io.Writer

internal class CustomTokenParser(
    private val tagName: String,
    private val callback: TagContext.() -> String,
    private val viewRenderer: ViewRenderer
) :
    TokenParser {

    override fun getTag() = tagName

    override fun parse(token: Token, parser: Parser): RenderableNode {
        val stream = parser.stream
        val lineNumber = token.lineNumber
        stream.next() // skip the token
        stream.expect(Token.Type.EXECUTE_END)
        return CustomNode(lineNumber, callback, viewRenderer)
    }
}

internal class CustomNode(
    lineNumber: Int,
    private val callback: TagContext.() -> String,
    private val viewRenderer: ViewRenderer
) : AbstractRenderableNode(lineNumber) {
    override fun render(self: PebbleTemplateImpl, writer: Writer, context: EvaluationContextImpl) {
        val call = context.httpCall
        val text = callback(TagContext(call, context, lineNumber, self.name))
        writer.write(viewRenderer.renderTemplate(text, SharedDataBag(), context.allScopedArgs()))
    }

    override fun accept(visitor: NodeVisitor) {
        visitor.visit(this)
    }
}

fun EvaluationContextImpl.allScopedArgs(): Map<String, Any?> {
    return scopeChain.globalScopes.map { scope ->
        scope.keys.map { it to scope.get(it) }.toMap()
    }.reduce { acc, map -> acc + map }
}
