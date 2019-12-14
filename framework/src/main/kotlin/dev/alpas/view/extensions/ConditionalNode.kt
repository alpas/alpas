package dev.alpas.view.extensions

import com.mitchellbosecke.pebble.extension.NodeVisitor
import com.mitchellbosecke.pebble.node.AbstractRenderableNode
import com.mitchellbosecke.pebble.node.BodyNode
import com.mitchellbosecke.pebble.template.EvaluationContextImpl
import com.mitchellbosecke.pebble.template.PebbleTemplateImpl
import dev.alpas.http.HttpCall
import java.io.Writer

class ConditionalNode(
    lineNumber: Int,
    private val trueBody: BodyNode,
    private val falseBody: BodyNode? = null,
    private val condition: (call: HttpCall) -> Boolean
) :
    AbstractRenderableNode(lineNumber) {

    override fun render(self: PebbleTemplateImpl, writer: Writer, context: EvaluationContextImpl) {
        if (condition(context.getVariable("call") as HttpCall)) {
            trueBody.render(self, writer, context)
        } else falseBody?.render(self, writer, context)
    }

    override fun accept(visitor: NodeVisitor) {
        visitor.visit(this)
    }
}
