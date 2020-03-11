package dev.alpas.view

import com.mitchellbosecke.pebble.extension.Function
import com.mitchellbosecke.pebble.extension.escaper.SafeString
import com.mitchellbosecke.pebble.template.EvaluationContext
import com.mitchellbosecke.pebble.template.EvaluationContextImpl
import com.mitchellbosecke.pebble.template.PebbleTemplate
import dev.alpas.validation.SharedDataBag
import dev.alpas.view.extensions.allScopedArgs

class CustomFunction(
    private val argNames: List<String>?,
    private val callback: FunctionContext.() -> String,
    private val viewRenderer: ViewRenderer
) :
    Function {
    override fun getArgumentNames(): List<String>? {
        return argNames
    }

    override fun execute(
        args: Map<String, Any>?,
        self: PebbleTemplate,
        context: EvaluationContext,
        lineNumber: Int
    ): Any {
        val call = context.httpCall
        val text = callback(
            FunctionContext(
                call,
                args,
                context,
                lineNumber,
                self.name
            )
        )
        val rendered =
            viewRenderer.renderTemplate(text,
                SharedDataBag(), (context as EvaluationContextImpl).allScopedArgs())
        return SafeString(rendered)
    }
}
