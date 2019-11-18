package dev.alpas.view.extensions

import com.mitchellbosecke.pebble.extension.Function
import com.mitchellbosecke.pebble.template.EvaluationContext
import com.mitchellbosecke.pebble.template.PebbleTemplate

internal class OldFunction : Function {
    override fun getArgumentNames(): List<String> {
        return listOf("key", "default")
    }

    @Suppress("UNCHECKED_CAST")
    override fun execute(args: Map<String, Any>?, self: PebbleTemplate, ctx: EvaluationContext, line: Int): Any {
        val name = args?.get("key") as String? ?: throw Exception("old() requires a param key.")
        val default = args?.get("default")
        val oldInputs = ctx.getVariable("_old_inputs") as? Map<*, List<Any>>
        val value = oldInputs?.get(name)
        if (value?.size == 1) {
            return value.firstOrNull() ?: default ?: ""
        }
        return value ?: default ?: ""
    }
}
