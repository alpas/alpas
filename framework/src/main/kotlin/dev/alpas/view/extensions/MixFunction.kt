package dev.alpas.view.extensions

import com.mitchellbosecke.pebble.extension.Function
import com.mitchellbosecke.pebble.template.EvaluationContext
import com.mitchellbosecke.pebble.template.PebbleTemplate
import dev.alpas.view.Mix

internal class MixFunction(private val mix: Mix) : Function {
    override fun getArgumentNames(): List<String> {
        return listOf("name", "directory")
    }

    override fun execute(args: Map<String, Any>?, self: PebbleTemplate, ctx: EvaluationContext, line: Int): Any {
        val name = args?.get("name") as String? ?: throw Exception("Mix requires name of the asset")
        val directory = args?.get("directory") as String?
        return mix(name, directory ?: "")
    }
}
