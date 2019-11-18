package dev.alpas.view.extensions

import com.mitchellbosecke.pebble.extension.NodeVisitor
import com.mitchellbosecke.pebble.lexer.Token
import com.mitchellbosecke.pebble.node.AbstractRenderableNode
import com.mitchellbosecke.pebble.node.RenderableNode
import com.mitchellbosecke.pebble.parser.Parser
import com.mitchellbosecke.pebble.template.EvaluationContextImpl
import com.mitchellbosecke.pebble.template.PebbleTemplateImpl
import com.mitchellbosecke.pebble.tokenParser.TokenParser
import dev.alpas.session.csrfSessionKey
import java.io.Writer

class CsrfTokenParser : TokenParser {

    override fun getTag() = "csrf"

    override fun parse(token: Token, parser: Parser): RenderableNode {
        val stream = parser.stream
        val lineNumber = token.lineNumber
        // skip the csrf token
        stream.next()
        stream.expect(Token.Type.EXECUTE_END)
        return CsrfTokenTag(lineNumber)
    }
}

class CsrfTokenTag(lineNumber: Int) : AbstractRenderableNode(lineNumber) {
    override fun render(self: PebbleTemplateImpl?, writer: Writer, context: EvaluationContextImpl) {
        val csrf = context.getVariable(csrfSessionKey)
        writer.write("""<input type="hidden" name="$csrfSessionKey" value="$csrf">""")
    }

    override fun accept(visitor: NodeVisitor) {
        visitor.visit(this)
    }
}

