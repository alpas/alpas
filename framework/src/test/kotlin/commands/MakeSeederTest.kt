package dev.alpas.tests.commands

import dev.alpas.SRC_DIR_KEY
import dev.alpas.ozone.console.MakeSeederCommand
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MakeSeederTest {
    @Test
    fun `can create one seeder`(@TempDir tempDir: Path) {
        val tempPath = tempDir.toAbsolutePath().toString()
        System.setProperty(SRC_DIR_KEY, tempPath)
        MakeSeederCommand("dev.alpas.tests")
            .main(arrayOf("TestSeeder", "--quiet"))

        val directory = File(tempPath, "database/seeds")
        assertTrue(directory.exists())
        val factory = File(directory, "TestSeeder.kt")
        assertTrue(factory.exists())

        assertEquals(exampleStub, factory.readText())
    }

    @Test
    fun `can create multiple seeders`(@TempDir tempDir: Path) {
        val tempPath = tempDir.toAbsolutePath().toString()
        System.setProperty(SRC_DIR_KEY, tempPath)

        MakeSeederCommand("dev.alpas.tests")
            .main(arrayOf("TestSeeder", "OtherSeeder", "--quiet"))

        val directory = File(tempPath, "database/seeds")
        val testSeeder = File(directory, "TestSeeder.kt")
        assertTrue(testSeeder.exists())

        val otherSeeder = File(directory, "OtherSeeder.kt")
        assertTrue(otherSeeder.exists())

        assertEquals(exampleStub.replace("Test", "Other"), otherSeeder.readText())
    }

    @Test
    fun `does not overwrite an existing seeder`(@TempDir tempDir: Path) {
        val tempPath = tempDir.toAbsolutePath().toString()
        System.setProperty(SRC_DIR_KEY, tempPath)

        MakeSeederCommand("dev.alpas.tests").main(arrayOf("TestSeeder", "--quiet"))

        val directory = File(tempPath, "database/seeds")
        val testSeeder = File(directory, "TestSeeder.kt")
        val comments = "// Add some comments"
        testSeeder.appendText(comments)

        MakeSeederCommand("dev.alpas.tests").main(arrayOf("TestSeeder", "--quiet"))

        assertTrue(testSeeder.readText().contains(comments))
    }

    @Test
    fun `can overwrite an existing seeder by setting --force flag`(@TempDir tempDir: Path) {
        val tempPath = tempDir.toAbsolutePath().toString()
        System.setProperty(SRC_DIR_KEY, tempPath)

        MakeSeederCommand("dev.alpas.tests").main(arrayOf("TestSeeder", "--quiet"))

        val directory = File(tempPath, "database/seeds")
        val testSeeder = File(directory, "TestSeeder.kt")
        val comments = "// Add some comments"
        testSeeder.appendText(comments)

        MakeSeederCommand("dev.alpas.tests").main(arrayOf("TestSeeder", "--quiet", "--force"))

        assertFalse(testSeeder.readText().contains(comments))
    }

    private val exampleStub = """
        package dev.alpas.tests.database.seeds

        import dev.alpas.Application
        import dev.alpas.ozone.Seeder

        internal class TestSeeder : Seeder() {
            override fun run(app: Application) {
                // Run your seeder(s) here
                // val users = from(UserFactory, "name" to "Jane Doe")
                
                // https://alpas.dev/docs/ozone
            }
        }
""".trimIndent()
}
