package dev.alpas.ozone.console.stubs

internal class MigrationStubs {
    companion object {
        fun createTableMigrationStub(): String {
            return """
                package StubPackageName

                import StubEntityPackageName.StubTableName
                import dev.alpas.ozone.migration.Migration

                class StubClazzName : Migration() {
                    override fun up() {
                        createTable(StubTableName)
                    }
                    override fun down() {
                        dropTable(StubTableName)
                    }
                }
            """.trimIndent()
        }

        fun modifyTableMigrationStub(): String {
            return """
                package StubPackageName

                import dev.alpas.ozone.migration.Migration

                class StubClazzName : Migration() {
                    override fun up() {
                        modify(StubTableName)
                    }
                    override fun down() {
                    }
                }
            """.trimIndent()
        }
    }
}
