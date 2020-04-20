package dev.alpas.queue.console.stubs

internal class Stubs {
    companion object {
        fun jobStub(): String {
            return """
                package StubPackageName

                import dev.alpas.Container
                import dev.alpas.queue.job.Job

                class StubClazzName : Job() {
                    override suspend fun invoke(container: Container) {
                    }
                }
            """.trimIndent()
        }

        fun queueTablesStub(): String {
            return """
                package StubPackageName

                import dev.alpas.ozone.migration.Migration
                import dev.alpas.queue.database.FailedJobRecords
                import dev.alpas.queue.database.JobRecords

                class StubClazzName : Migration() {
                    override val name = "StubJobName"
                    
                    override fun up() {
                        createTable(JobRecords)
                        createTable(FailedJobRecords)
                    }
                    override fun down() {
                        dropTable(FailedJobRecords)
                        dropTable(JobRecords)
                    }
                }
            """.trimIndent()
        }
    }
}
