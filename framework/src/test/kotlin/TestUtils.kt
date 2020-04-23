@file:Suppress("UNCHECKED_CAST")

package dev.alpas.tests

import dev.alpas.PackageClassLoader
import dev.alpas.routing.*
import io.mockk.Call
import io.mockk.MockKAnswerScope
import me.liuwj.ktorm.database.useConnection
import java.nio.file.Paths

fun withRouter(block: Router.() -> Unit): Router {
    return Router() {
        block()
        compile(PackageClassLoader("dev.alpas.tests"))
    }
}

fun Route?.controllerMethod(): String? {
    return when (val handler = this?.handler) {
        is DynamicControllerHandler -> handler.method
        is ControllerHandler -> handler.method
        else -> null
    }
}

class TestController : Controller()


fun String.scope(): MockKAnswerScope<String, String>.(Call) -> String {
    return {
        val args = it.invocation.args[0] as Array<String>
        Paths.get(this@scope, *args).toAbsolutePath().toString()
    }
}

open class BaseTest() {
    protected fun execSqlFile(filename: String) {
        useConnection { conn ->
            conn.createStatement().use {
                javaClass.classLoader
                    ?.getResourceAsStream(filename)?.let {
                        it.bufferedReader().use { reader ->
                            execSqlScript(reader.readText())
                        }
                    }
            }
        }
    }

    protected fun execSqlScript(script: String) {
        useConnection { conn ->
            conn.createStatement().use { statement ->
                for (sql in script.split(';')) {
                    if (sql.any { it.isLetterOrDigit() }) {
                        statement.executeUpdate(sql)
                    }
                }
            }
        }
    }
}
