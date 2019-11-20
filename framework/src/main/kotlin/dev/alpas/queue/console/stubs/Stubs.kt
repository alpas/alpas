package dev.alpas.queue.console.stubs

internal class Stubs {
    companion object {
        fun jobStub(): String {
            return """
                package StubPackageName

                import dev.alpas.Container
                import dev.alpas.queue.Job

                class StubClazzName : Job() {
                    override fun invoke(container: Container) {
                    }
                }
            """.trimIndent()
        }
    }
}
