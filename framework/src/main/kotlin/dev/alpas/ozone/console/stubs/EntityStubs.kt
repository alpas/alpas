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

                interface StubClazzName : OzoneEntity<StubClazzName> {
                    var id: Long
                    var name: String?
                    var createdAt: Instant?
                    var updatedAt: Instant?

                    companion object : OzoneEntity.Of<StubClazzName>()
                }
            """
        }

        private fun tableStub(): String {
            return """
                object StubTableClazzName : OzoneTable<StubClazzName>("StubTableName") {
                    val id by bigIncrements()
                    val name by string("name").size(150).nullable().bindTo { it.name }
                    val createdAt by createdAt()
                    val updatedAt by updatedAt()
                }
            """
        }
    }
}
