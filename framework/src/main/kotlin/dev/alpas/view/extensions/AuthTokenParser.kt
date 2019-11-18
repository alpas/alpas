package dev.alpas.view.extensions

import com.mitchellbosecke.pebble.error.ParserException
import com.mitchellbosecke.pebble.lexer.Token
import com.mitchellbosecke.pebble.node.BodyNode
import com.mitchellbosecke.pebble.node.RenderableNode
import com.mitchellbosecke.pebble.parser.Parser
import com.mitchellbosecke.pebble.tokenParser.TokenParser

class AuthTokenParser(private val tag: String) : TokenParser {
    private val endTag = "end$tag"

    override fun parse(token: Token, parser: Parser): RenderableNode {
        val stream = parser.stream
        val lineNumber = token.lineNumber

        // skip the 'auth' token
        stream.next()

        stream.expect(Token.Type.EXECUTE_END)

        val authBody = parser.subparse(decideIfFork)

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
        return if (getTag() == "auth")
            AuthNode(lineNumber, authBody, elseBody)
        else
            GuestNode(lineNumber, authBody, elseBody)
    }

    override fun getTag() = tag

    private val decideIfFork = { token: Token -> token.test(Token.Type.NAME, "else", endTag) }
}
