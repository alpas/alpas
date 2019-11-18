package dev.alpas.view.extensions

import com.mitchellbosecke.pebble.extension.NodeVisitor
import com.mitchellbosecke.pebble.node.AbstractRenderableNode
import com.mitchellbosecke.pebble.node.BodyNode
import com.mitchellbosecke.pebble.template.EvaluationContextImpl
import com.mitchellbosecke.pebble.template.PebbleTemplateImpl
import dev.alpas.auth.AuthChannel
import java.io.Writer

class AuthNode(lineNumber: Int, private val authBody: BodyNode, private val elseBody: BodyNode? = null) :
    AbstractRenderableNode(lineNumber) {

    override fun render(self: PebbleTemplateImpl, writer: Writer, context: EvaluationContextImpl) {
        val isLoggedIn = (context.getVariable("auth") as? AuthChannel)?.isLoggedIn() ?: false
        if (isLoggedIn) {
            this.authBody.render(self, writer, context)
        } else if (this.elseBody != null) {
            this.elseBody.render(self, writer, context)
        }
    }

    override fun accept(visitor: NodeVisitor) {
        visitor.visit(this)
    }
}

class GuestNode(lineNumber: Int, private val authBody: BodyNode, private val elseBody: BodyNode? = null) :
    AbstractRenderableNode(lineNumber) {

    override fun render(self: PebbleTemplateImpl, writer: Writer, context: EvaluationContextImpl) {
        val isLoggedIn = (context.getVariable("auth") as? AuthChannel)?.isLoggedIn() ?: false
        if (!isLoggedIn) {
            this.authBody.render(self, writer, context)
        } else if (this.elseBody != null) {
            this.elseBody.render(self, writer, context)
        }
    }

    override fun accept(visitor: NodeVisitor) {
        visitor.visit(this)
    }
}
