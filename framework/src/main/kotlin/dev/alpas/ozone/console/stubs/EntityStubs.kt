package dev.alpas.ozone.console.stubs

internal class EntityStubs {
    companion object {
        fun simpleStub(): String {
            return """
                ${simpleEntityStub()}
                ${simpleTableStub()}
            """.trimIndent()
        }

        private fun simpleEntityStub(): String {
            return """
                package StubPackageName
                
                import me.liuwj.ktorm.dsl.QueryRowSet
                import me.liuwj.ktorm.schema.BaseTable
                import dev.alpas.ozone.MigratingTable
                import me.liuwj.ktorm.schema.long
                import me.liuwj.ktorm.schema.timestamp
                import java.time.Instant
                
                data class StubClazzName(val id: Long, val createdAt: Instant?, val updatedAt: Instant?)
            """
        }

        private fun simpleTableStub(): String {
            return """
                object StubTableClazzName : MigratingTable<StubClazzName>("StubTableName") {
                    val id by long("id").primaryKey()
                    val createdAt by timestamp("created_at")
                    val updatedAt by timestamp("updated_at")

                    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean): StubClazzName {
                        return StubClazzName(id = row[id] ?: 0, createdAt = row[createdAt], updatedAt = row[updatedAt])
                    }
                }
            """
        }

        fun stub(): String {
            return """
                ${entityStub()}
                ${tableStub()}
            """.trimIndent()
        }

        private fun entityStub(): String {
            return """
                package StubPackageName
                
                import me.liuwj.ktorm.entity.Entity
                import dev.alpas.ozone.MigratingTable
                import me.liuwj.ktorm.schema.long
                import me.liuwj.ktorm.schema.timestamp
                import java.time.Instant

                interface StubClazzName : Entity<StubClazzName> {
                    val id: Long
                    val createdAt: Instant?
                    val updatedAt: Instant?

                    companion object : Entity.Factory<StubClazzName>()
                }
            """.trimIndent()
        }

        private fun tableStub(): String {
            return """
                object StubTableClazzName : MigratingTable<StubClazzName>("StubTableName") {
                    val id by long("id").primaryKey().bindTo { it.id }
                    val createdAt by timestamp("created_at").nullable().bindTo { it.createdAt }
                    val updatedAt by timestamp("updated_at").nullable().bindTo { it.updatedAt }
                }
            """.trimIndent()
        }
    }
}
