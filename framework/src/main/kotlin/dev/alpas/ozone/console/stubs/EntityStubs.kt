package dev.alpas.ozone.console.stubs

internal class EntityStubs {
    companion object {
        fun stub(): String {
            return """${entityStub()} ${tableStub()}""".trimIndent()
        }

        private fun entityStub(): String {
            return """
                package StubPackageName
                
                import dev.alpas.ozone.*
                import java.time.Instant

                interface StubClazzName : Ozone<StubClazzName> {
                    var id: Long
                    var createdAt: Instant?
                    var updatedAt: Instant?

                    companion object : Ozone.Of<StubClazzName>()
                }
            """
        }

        private fun tableStub(): String {
            return """
                object StubTableClazzName : OzoneTable<StubClazzName>("StubTableName") {
                    val id by bigIncrements().bindTo { it.id }
                    val createdAt by createdAt()
                    val updatedAt by updatedAt()
                }
            """
        }
    }
}
