package dev.alpas.mailing

import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.loader.ClasspathLoader
import com.mitchellbosecke.pebble.loader.FileLoader
import com.mitchellbosecke.pebble.loader.Loader
import dev.alpas.STORAGE_TEMPLATES_PATH
import java.io.File
import java.io.StringWriter

class EmailRenderer(private val storageTemplatesPath: String? = null) {
    private val engine: PebbleEngine by lazy { engineBuilder.cacheActive(false).autoEscaping(false).build() }
    private val engineBuilder by lazy { PebbleEngine.Builder().loader(loader()) }

    private fun loader(): Loader<String> {
        val templatesPath = storageTemplatesPath ?: System.getProperty(STORAGE_TEMPLATES_PATH)
        if (File(templatesPath).exists()) {
            return FileLoader().apply { prefix = templatesPath }
        }
        return ClasspathLoader().apply { prefix = "templates" }
    }

    fun renderEmail(viewName: String, emailArgs: Map<String, Any?>?, extension: String = "peb"): String {
        return StringWriter().use { writer ->
            val template = engine.getTemplate("${viewName}.${extension}")
            template.evaluate(writer, emailArgs)
            writer.toString()
        }
    }
}
