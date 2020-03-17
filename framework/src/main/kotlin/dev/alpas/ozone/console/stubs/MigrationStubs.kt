package dev.alpas.ozone.console.stubs

internal class MigrationStubs {
    companion object {
        fun createTableMigrationStub(): String {
            return """
                package StubPackageName

                import StubEntityPackageName.StubTableName
                import dev.alpas.ozone.migration.Migration

                class StubClazzName : Migration() {
                    override val name = "StubMigrationName"
                    
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

                import StubEntityPackageName.StubTableName
                import dev.alpas.ozone.migration.Migration

                class StubClazzName : Migration() {
                    override val name = "StubMigrationName"
                    
                    override fun up() {
                        modifyTable(StubTableName) {
                            // addColumn(StubTableName.newColumn).after(StubTableName.anotherColumn)
                        }
                    }
                    
                    override fun down() {
                        modifyTable(StubTableName) {
                            // dropColumn("oldColumn1", "oldColumn2")
                        }
                    }
                }
            """.trimIndent()
        }
    }
}
