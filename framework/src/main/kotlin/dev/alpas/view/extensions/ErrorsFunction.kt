package dev.alpas.view.extensions

import com.mitchellbosecke.pebble.extension.Function
import com.mitchellbosecke.pebble.template.EvaluationContext
import com.mitchellbosecke.pebble.template.PebbleTemplate

internal class ErrorsFunction : Function {
    override fun getArgumentNames(): List<String> {
        return listOf("key", "default")
    }

    @Suppress("UNCHECKED_CAST")
    override fun execute(args: Map<String, Any>?, self: PebbleTemplate, ctx: EvaluationContext, line: Int): Any {
        // if no args provided then we'll assume that the user just wants to get the errors map
        if (args.isNullOrEmpty()) return (ctx.getVariable("errors") ?: mapOf<String, Any>())

        val name = args["key"] as String
        val default = args["default"]
        val errors = ctx.getVariable("errors") as? Map<*, ArrayList<Any>>
        val value = errors?.get(name)
        if (value?.size == 1) {
            return value.firstOrNull() ?: default ?: ""
        }
        return value ?: default ?: ""
    }
}

internal class FirstErrorFunction : Function {
    override fun getArgumentNames(): List<String> {
        return listOf("key", "default")
    }

    @Suppress("UNCHECKED_CAST")
    override fun execute(args: Map<String, Any>?, self: PebbleTemplate, ctx: EvaluationContext, line: Int): Any {
        if (args.isNullOrEmpty()) throw Exception("firstError() requires the error key.")

        val name = args["key"] as String
        val errors = ctx.getVariable("errors") as? Map<*, ArrayList<Any>>
        val value = errors?.get(name)
        return value?.firstOrNull() ?: args["default"] ?: ""
    }
}

internal class HasErrorFunction : Function {
    override fun getArgumentNames(): List<String> {
        return listOf("key")
    }

    @Suppress("UNCHECKED_CAST")
    override fun execute(args: Map<String, Any>?, self: PebbleTemplate, ctx: EvaluationContext, line: Int): Any {
        if (args.isNullOrEmpty()) throw Exception("hasError() requires the error key.")

        val name = args["key"] as String
        val errors = ctx.getVariable("errors") as? Map<*, ArrayList<Any>>
        return errors?.containsKey(name) == true
    }
}

internal class WhenErrorFunction : Function {
    override fun getArgumentNames(): List<String> {
        return listOf("field", "apply")
    }

    @Suppress("UNCHECKED_CAST")
    override fun execute(args: Map<String, Any>?, self: PebbleTemplate, ctx: EvaluationContext, line: Int): Any? {
        if (args.isNullOrEmpty()) throw Exception("whenError() requires the error key.")

        val field = args["field"] as String
        val errors = ctx.getVariable("errors") as? Map<*, ArrayList<Any>>
        return if (errors?.containsKey(field) == true) {
            args["apply"]
        } else {
            ""
        }
    }
}
