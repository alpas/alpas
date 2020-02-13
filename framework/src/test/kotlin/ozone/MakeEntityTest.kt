package dev.alpas.tests.ozone

import dev.alpas.SRC_DIR_KEY
import dev.alpas.ozone.console.MakeEntityCommand
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MakeEntityTest {
    @Test
    fun `can create an entity`(@TempDir tempDir: Path) {
        val tempPath = tempDir.toAbsolutePath().toString()
        System.setProperty(SRC_DIR_KEY, tempPath)
        MakeEntityCommand("dev.alpas.tests")
            .main(arrayOf("Test", "--quiet"))

        val directory = File(tempPath, "entities")
        assertTrue(directory.exists())
        val entity = File(directory, "Test.kt")
        assertTrue(entity.exists())

        assertEquals(entityStub, entity.readText())
    }

    @Test
    fun `can create multiple entities`(@TempDir tempDir: Path) {
        val tempPath = tempDir.toAbsolutePath().toString()
        System.setProperty(SRC_DIR_KEY, tempPath)

        MakeEntityCommand("dev.alpas.tests")
            .main(arrayOf("Test", "Other", "--quiet"))

        val directory = File(tempPath, "entities")
        val entity = File(directory, "Test.kt")
        assertTrue(entity.exists())

        val otherSeeder = File(directory, "Other.kt")
        assertTrue(otherSeeder.exists())

        assertEquals(
            entityStub.replace("Test", "Other").replace(""""tests"""", """"others""""),
            otherSeeder.readText()
        )
    }

    @Test
    fun `does not overwrite an existing entity`(@TempDir tempDir: Path) {
        val tempPath = tempDir.toAbsolutePath().toString()
        System.setProperty(SRC_DIR_KEY, tempPath)

        MakeEntityCommand("dev.alpas.tests").main(arrayOf("Test", "--quiet"))

        val directory = File(tempPath, "entities")
        val entity = File(directory, "Test.kt")
        val comments = "// Add some comments"
        entity.appendText(comments)

        MakeEntityCommand("dev.alpas.tests").main(arrayOf("Test", "--quiet"))

        assertTrue(entity.readText().contains(comments))
    }

    @Test
    fun `can overwrite an existing entity by setting --force flag`(@TempDir tempDir: Path) {
        val tempPath = tempDir.toAbsolutePath().toString()
        System.setProperty(SRC_DIR_KEY, tempPath)

        MakeEntityCommand("dev.alpas.tests").main(arrayOf("Test", "--quiet"))

        val directory = File(tempPath, "entities")
        val entity = File(directory, "Test.kt")
        val comments = "// Add some comments"
        entity.appendText(comments)

        MakeEntityCommand("dev.alpas.tests").main(arrayOf("Test", "--quiet", "--force"))

        assertFalse(entity.readText().contains(comments))
    }

    @Test
    fun `can create an entity with a migration`(@TempDir tempDir: Path) {
        val tempPath = tempDir.toAbsolutePath().toString()
        System.setProperty(SRC_DIR_KEY, tempPath)
        MakeEntityCommand("dev.alpas.tests")
            .main(arrayOf("Test", "-m", "--quiet"))

        val entitiesDir = File(tempPath, "entities")
        assertTrue(entitiesDir.exists())
        val entity = File(entitiesDir, "Test.kt")
        assertTrue(entity.exists())
        assertEquals(entityStub, entity.readText())

        val migrationsDir = File(tempPath, "database/migrations")
        assertTrue(migrationsDir.exists())
        val files = migrationsDir.listFiles { filter ->
            filter.name.endsWith("_create_tests_table.kt")
        }

        assertNotNull(files)
        assertEquals(1, files?.count())

        val migration = files?.first()!!
        assertEquals(migrationStub, migration.readText())
    }

    private val entityStub = """
        package dev.alpas.tests.entities

        import dev.alpas.ozone.*
        import java.time.Instant

        interface Test : Ozone<Test> {
            var id: Long
            var createdAt: Instant?
            var updatedAt: Instant?

            companion object : Ozone.Of<Test>()
        }

        object Tests : OzoneTable<Test>("tests") {
            val id by bigIncrements().bindTo { it.id }
            val createdAt by createdAt()
            val updatedAt by updatedAt()
        }
""".trimIndent()

    private val migrationStub = """
        package dev.alpas.tests.database.migrations

        import dev.alpas.tests.entities.Tests
        import dev.alpas.ozone.migration.Migration

        class CreateTestsTable : Migration() {
            override fun up() {
                createTable(Tests)
            }
            
            override fun down() {
                dropTable(Tests)
            }
        }
    """.trimIndent()
}
