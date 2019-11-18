package dev.alpas.view.extensions

import com.mitchellbosecke.pebble.extension.Function
import com.mitchellbosecke.pebble.template.EvaluationContext
import com.mitchellbosecke.pebble.template.PebbleTemplate

internal class FlashFunction : Function {
    override fun getArgumentNames(): List<String> {
        return listOf("key", "default")
    }

    @Suppress("UNCHECKED_CAST")
    override fun execute(args: Map<String, Any>?, self: PebbleTemplate, ctx: EvaluationContext, line: Int): Any {
        // if no args provided then we'll assume that the user just wants to get the flash map
        if (args.isNullOrEmpty()) return (ctx.getVariable("_flash") ?: mapOf<String, Any?>())

        val name = args["key"] as String
        val default = args["default"]
        val flash = ctx.getVariable("_flash") as? Map<String, Any?>
        val value = flash?.get(name)
        return value ?: default ?: ""
    }
}

internal class HasFlashFunction : Function {
    override fun getArgumentNames(): List<String> {
        return listOf("key")
    }

    @Suppress("UNCHECKED_CAST")
    override fun execute(args: Map<String, Any>?, self: PebbleTemplate, ctx: EvaluationContext, line: Int): Any {
        if (args.isNullOrEmpty()) throw Exception("hasFlash() requires the flash key.")

        val name = args["key"] as String
        val flash = ctx.getVariable("_flash") as? Map<*, Any?>
        return flash?.containsKey(name) == true
    }
}

