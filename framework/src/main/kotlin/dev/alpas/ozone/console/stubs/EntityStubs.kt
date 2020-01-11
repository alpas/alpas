package dev.alpas.ozone.console.stubs

internal class EntityStubs {
    companion object {
        fun stub(withTable: Boolean = true): String {
            val entityStub = entityStub()
            return if (withTable) {
                """
                $entityStub 
                   ${tableStub()}
                """.trimIndent()
            } else {
                entityStub
            }
        }

        private fun entityStub(): String {
            return """package StubPackageName
                import org.jetbrains.exposed.dao.LongEntity
                import org.jetbrains.exposed.dao.LongEntityClass
                import org.jetbrains.exposed.dao.id.EntityID
                import org.jetbrains.exposed.dao.id.LongIdTable
                import org.jetbrains.exposed.sql.`java-time`.timestamp

                class StubClazzName(id: EntityID<Long>) : LongEntity(id) {
                    companion object : LongEntityClass<StubClazzName>(StubTableClazzName)

                    val createdAt by StubTableClazzName.createdAt
                    val updatedAt by StubTableClazzName.updatedAt
                }
            """.trimIndent()
        }

        private fun tableStub(): String {
            return """
                object StubTableClazzName : LongIdTable("StubTableName") {
                    val createdAt = timestamp("created_at")
                    val updatedAt = timestamp("updated_at")
                }
            """.trimIndent()
        }
    }
}
