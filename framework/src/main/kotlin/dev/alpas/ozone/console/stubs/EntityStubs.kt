package dev.alpas.ozone.console.stubs

internal class EntityStubs {
    companion object {
        fun stub(withTable: Boolean = true): String {
            val entityStub = entityStub()
            val tableStub = if (withTable) tableStub() else ""
            return """
               $tableStub
               $entityStub 
            """.trimIndent()
        }

        private fun entityStub(): String {
            return """
                package StubPackageName
                
                import org.jetbrains.exposed.dao.LongEntity
                import org.jetbrains.exposed.dao.LongEntityClass
                import org.jetbrains.exposed.dao.id.EntityID
                import org.jetbrains.exposed.dao.id.LongIdTable
                import org.jetbrains.exposed.sql.`java-time`.timestamp

                class StubClazzName(id: EntityID<Long>) : LongEntity(id) {
                    companion object : LongEntityClass<StubClazzName>(StubTableClazzName)

                    var createdAt by StubTableClazzName.createdAt
                    var updatedAt by StubTableClazzName.updatedAt
                }
            """
        }

        private fun tableStub(): String {
            return """
                object StubTableClazzName : LongIdTable("StubTableName") {
                    val createdAt = timestamp("created_at")
                    val updatedAt = timestamp("updated_at")
                }
            """
        }
    }
}
