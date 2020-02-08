package dev.alpas.ozone.console.stubs

internal class EntityStubs {
    companion object {
        fun simpleStub(): String {
            return """${simpleEntityStub()} ${simpleTableStub()}""".trimIndent()
        }

        private fun simpleEntityStub(): String {
            return """
                package StubPackageName
                
                import dev.alpas.ozone.bigIncrements
                import me.liuwj.ktorm.dsl.QueryRowSet
                import me.liuwj.ktorm.schema.BaseTable
                import dev.alpas.ozone.MigratingTable
                import me.liuwj.ktorm.schema.timestamp
                import java.time.Instant
                
                data class StubClazzName(val id: Long, val createdAt: Instant?, val updatedAt: Instant?)
            """
        }

        private fun simpleTableStub(): String {
            return """
                object StubTableClazzName : MigratingTable<StubClazzName>("StubTableName") {
                    var id by bigIncrements("id")
                    var createdAt by timestamp("created_at")
                    var updatedAt by timestamp("updated_at")

                    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean): StubClazzName {
                        return StubClazzName(id = row[id] ?: 0, createdAt = row[createdAt], updatedAt = row[updatedAt])
                    }
                }
            """
        }

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
