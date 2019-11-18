package dev.alpas.ozone.console.stubs

internal class MigrationStubs {
    companion object {
        fun createTableMigrationStub(): String {
            return """
                package StubPackageName

                import Migration

                class StubClazzName : Migration() {
                    override fun up() {
                        createTable("StubTableName") {
                            bigIncrements("id")
                            timestamps()
                        }
                    }
                    override fun down() {
                        dropTable("StubTableName")
                    }
                }
            """.trimIndent()
        }

        fun modifyTableMigrationStub(): String {
            return """
                package StubPackageName

                import Migration

                class StubClazzName : Migration() {
                    override fun up() {
                        addVarcharColumn("StubTableName", "column_name")
                    }
                    override fun down() {
                        removeColumn("StubTableName", "column_name")
                    }
                }
            """.trimIndent()
        }
    }
}
