package dev.alpas.view.extensions

import com.github.marlonlom.utilities.timeago.TimeAgo
import com.mitchellbosecke.pebble.extension.Filter
import com.mitchellbosecke.pebble.template.EvaluationContext
import com.mitchellbosecke.pebble.template.PebbleTemplate
import dev.alpas.JsonSerializer
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class IntFilter : Filter {
    override fun apply(
        input: Any?,
        args: MutableMap<String, Any>?,
        self: PebbleTemplate?,
        context: EvaluationContext?,
        lineNumber: Int
    ): Any? {
        return input?.toString()?.toIntOrNull()
    }

    override fun getArgumentNames(): List<String>? {
        return null
    }
}

class JsonEncodeFilter : Filter {
    override fun getArgumentNames(): List<String>? {
        return null
    }

    override fun apply(
        input: Any?,
        args: MutableMap<String, Any>?,
        self: PebbleTemplate?,
        context: EvaluationContext?,
        lineNumber: Int
    ): Any? {
        return JsonSerializer.serialize(input)
    }
}

class AgoFilter : Filter {
    override fun getArgumentNames(): List<String>? {
        return null
    }

    override fun apply(
        input: Any?,
        args: MutableMap<String, Any>?,
        self: PebbleTemplate,
        context: EvaluationContext?,
        lineNumber: Int
    ): Any? {
        return when (input) {
            is Instant -> TimeAgo.using(input.toEpochMilli())
            // todo: extract zoneoffset from the context and use it here
            is LocalDateTime -> TimeAgo.using(input.toInstant(ZoneOffset.UTC).toEpochMilli())
            else -> {
                val inputType = input?.javaClass?.name
                throw Exception("Ago requires an Instant or LocalDateTime object but is '${inputType}'. Called from ${self.name} line no. $lineNumber")
            }
        }
    }
}

