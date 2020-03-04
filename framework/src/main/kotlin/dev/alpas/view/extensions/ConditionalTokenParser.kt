package dev.alpas.view.extensions

import com.mitchellbosecke.pebble.error.ParserException
import com.mitchellbosecke.pebble.extension.NodeVisitor
import com.mitchellbosecke.pebble.lexer.Token
import com.mitchellbosecke.pebble.node.AbstractRenderableNode
import com.mitchellbosecke.pebble.node.BodyNode
import com.mitchellbosecke.pebble.node.RenderableNode
import com.mitchellbosecke.pebble.parser.Parser
import com.mitchellbosecke.pebble.template.EvaluationContextImpl
import com.mitchellbosecke.pebble.template.PebbleTemplateImpl
import com.mitchellbosecke.pebble.tokenParser.TokenParser
import dev.alpas.view.TagContext
import dev.alpas.view.httpCall
import java.io.Writer

internal class ConditionalTokenParser(
    private val tag: String,
    private val condition: (context: TagContext) -> Boolean
) :
    TokenParser {
    private val endTag = "end$tag"

    override fun parse(token: Token, parser: Parser): RenderableNode {
        val stream = parser.stream
        val lineNumber = token.lineNumber

        // skip the token
        stream.next()

        stream.expect(Token.Type.EXECUTE_END)

        val ifBody = parser.subparse(decideIfFork)

        var elseBody: BodyNode? = null
        var end = false
        while (!end) {
            if (stream.current().value == null) {
                throw ParserException(
                    null,
                    "Unexpected end of template. Was looking for the \"$endTag\" tag",
                    stream.current().lineNumber, stream.filename
                )
            }

            when (stream.current().value) {
                "else" -> {
                    stream.next()
                    stream.expect(Token.Type.EXECUTE_END)
                    elseBody = parser.subparse { tkn -> tkn.test(Token.Type.NAME, endTag) }
                }

                endTag -> {
                    stream.next()
                    end = true
                }
                else -> throw ParserException(
                    null,
                    "Unexpected end of template. Was looking for the following tags \"else\", or \"$endTag\"",
                    stream.current().lineNumber, stream.filename
                )
            }
        }

        stream.expect(Token.Type.EXECUTE_END)
        return ConditionalNode(lineNumber, ifBody, elseBody, condition)
    }

    override fun getTag() = tag

    private val decideIfFork = { token: Token -> token.test(Token.Type.NAME, "else", endTag) }
}

internal class ConditionalNode(
    lineNumber: Int,
    private val trueBody: BodyNode,
    private val falseBody: BodyNode? = null,
    private val condition: (context: TagContext) -> Boolean
) :
    AbstractRenderableNode(lineNumber) {

    override fun render(self: PebbleTemplateImpl, writer: Writer, context: EvaluationContextImpl) {
        val tagContext = TagContext(context.httpCall, context, lineNumber, self.name)

        if (condition(tagContext)) {
            trueBody.render(self, writer, context)
        } else {
            falseBody?.render(self, writer, context)
        }
    }

    override fun accept(visitor: NodeVisitor) {
        visitor.visit(this)
    }
}
