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

        fun validationRuleStub(): String {
            return """
                package StubPackageName
                
                import dev.alpas.validation.ErrorMessage
                import dev.alpas.validation.Rule
                import dev.alpas.validation.ValidationGuard

                class StubClazzName(private val message: ErrorMessage = null) : Rule() {
                    override fun check(attribute: String, value: Any?): Boolean {
                        // todo: perform your actual validation here
                        val isValid = value != null

                        if (!isValid) {
                            // todo: set the proper error message
                            error = message?.let { it(attribute, value) } ?: "StubClazzName validation failed."
                        }
                        return isValid
                    }
                }

                fun ValidationGuard.StubFunctionName(message: ErrorMessage = null): Rule {
                    return rule(StubClazzName(message))
                }
            """.trimIndent()
        }
    }
}
