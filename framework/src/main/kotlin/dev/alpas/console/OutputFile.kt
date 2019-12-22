package dev.alpas.console

import dev.alpas.extensions.toPascalCase
import java.io.File

class OutputFile {
    private var packageName: String? = null
    var clazzName: String? = null
    private var replacements = mapOf<String, String>()
    lateinit var target: File
        private set
    private var stub: String = ""
    fun dump() {
        if (!makeParentDirs()) {
            throw Exception("Couldn't create ${target.parent} directory.")
        }
        val clazz = (clazzName ?: target.nameWithoutExtension).toPascalCase()
        var contents = stub.replace("StubClazzName", clazz)
        packageName?.let {
            contents = contents.replace("StubPackageName", it)
        }
        replacements.forEach { (placeholder, value) ->
            contents = contents.replace(placeholder, value)
        }
        target.writeText(contents)
    }

    private fun makeParentDirs(): Boolean {
        return target.parentFile.exists() || target.parentFile.mkdirs()
    }

    fun target(target: File): OutputFile {
        this.target = target
        return this
    }

    fun exists(): Boolean {
        return target.exists()
    }

    fun packageName(name: String): OutputFile {
        packageName = name
        return this
    }

    fun className(name: String): OutputFile {
        clazzName = name
        return this
    }

    fun stub(stub: String): OutputFile {
        this.stub = stub
        return this
    }

    fun replacements(replacements: Map<String, String>): OutputFile {
        this.replacements = replacements
        return this
    }
}
