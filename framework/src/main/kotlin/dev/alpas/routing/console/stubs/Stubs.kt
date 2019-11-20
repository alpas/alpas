package dev.alpas.routing.console.stubs

internal class Stubs {
    companion object {
        fun controllerPlainStub(): String {
            return """
                package StubPackageName

                import dev.alpas.http.HttpCall
                import dev.alpas.routing.Controller

                class StubClazzName : Controller() {
                    fun index(call: HttpCall) {
                        call.reply("Hello, StubClazzName!")
                    }
                }
            """.trimIndent()
        }

        fun middlewareStub(): String {
            return """
                package StubPackageName

                import dev.alpas.Handler
                import dev.alpas.Middleware
                import dev.alpas.http.HttpCall

                class StubClazzName : Middleware<HttpCall>() {
                    override fun invoke(call: HttpCall, forward: Handler<HttpCall>) {
                        forward(call)
                    }
                }
            """.trimIndent()
        }

        fun validationGuardStub(): String {
            return """
                package StubPackageName

                import dev.alpas.validation.ValidationGuard
                import dev.alpas.validation.Rule

                class StubClazzName : ValidationGuard() {
                    override fun rules(): Map<String, Iterable<Rule>> {
                          TODO("Return list of rules for this guard. Something like:")
                          return mapOf("field" to listOf())
                    }
                }
            """.trimIndent()
        }
    }
}
