package dev.alpas.ozone.console.stubs

internal class SeederStubs {
    companion object {
        fun stub(): String {
            return """
                package StubPackageName

                import dev.alpas.Application
                import dev.alpas.ozone.Seeder

                internal class StubClazzName : Seeder() {
                    override fun run(app: Application) {
                        // Run your seeder(s) here
                        // val users = from(UserFactory, "name" to "Jane Doe")
                        
                        // https://alpas.dev/docs/ozone
                    }
                }
            """.trimIndent()
        }
    }
}
