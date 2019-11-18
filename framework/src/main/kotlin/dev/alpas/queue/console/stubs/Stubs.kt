package dev.alpas.queue.console.stubs

internal class Stubs {
    companion object {
        fun jobStub(): String {
            return """
                package StubPackageName

                import Container
                import Job

                class StubClazzName : Job() {
                    override fun invoke(container: Container) {
                    }
                }
            """.trimIndent()
        }
    }
}
