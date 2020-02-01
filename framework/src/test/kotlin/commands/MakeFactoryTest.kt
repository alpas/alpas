package dev.alpas.tests.commands

import dev.alpas.SRC_DIR_KEY
import dev.alpas.ozone.console.MakeFactoryCommand
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MakeFactoryTest {
    @Test
    fun `can create one factory`(@TempDir tempDir: Path) {
        val tempPath = tempDir.toAbsolutePath().toString()
        System.setProperty(SRC_DIR_KEY, tempPath)
        MakeFactoryCommand("dev.alpas.tests")
            .main(arrayOf("Test", "--quiet"))

        val directory = File(tempPath, "database/factories")
        assertTrue(directory.exists())
        val factory = File(directory, "TestFactory.kt")
        assertTrue(factory.exists())

        assertEquals(exampleStub, factory.readText())
    }

    @Test
    fun `can create factory for a factory`(@TempDir tempDir: Path) {
        val tempPath = tempDir.toAbsolutePath().toString()
        System.setProperty(SRC_DIR_KEY, tempPath)
        MakeFactoryCommand("dev.alpas.tests")
            .main(arrayOf("Factory", "--quiet"))

        val directory = File(tempPath, "database/factories")
        assertTrue(directory.exists())
        val factory = File(directory, "FactoryFactory.kt")
        assertTrue(factory.exists())
        val expected = exampleStub
            .replace("TestFactory", "FactoryFactory")
            .replace("Tests", "Factories")
            .replace("Test", "Factory")

        assertEquals(expected, factory.readText())
    }

    @Test
    fun `takes care of extra 'factory' suffix`(@TempDir tempDir: Path) {
        val tempPath = tempDir.toAbsolutePath().toString()
        System.setProperty(SRC_DIR_KEY, tempPath)
        MakeFactoryCommand("dev.alpas.tests")
            .main(arrayOf("TestFactory", "--quiet"))

        val directory = File(tempPath, "database/factories")
        assertTrue(directory.exists())
        val factory = File(directory, "TestFactory.kt")
        assertTrue(factory.exists())

        assertEquals(exampleStub, factory.readText())
    }

    @Test
    fun `can create multiple factories`(@TempDir tempDir: Path) {
        val tempPath = tempDir.toAbsolutePath().toString()
        System.setProperty(SRC_DIR_KEY, tempPath)

        MakeFactoryCommand("dev.alpas.tests")
            .main(arrayOf("Test", "Other", "--quiet"))

        val directory = File(tempPath, "database/factories")
        val testFactory = File(directory, "TestFactory.kt")
        assertTrue(testFactory.exists())

        val otherFactory = File(directory, "OtherFactory.kt")
        assertTrue(otherFactory.exists())

        assertEquals(exampleStub.replace("Test", "Other"), otherFactory.readText())
    }

    @Test
    fun `does not overwrite an existing factory`(@TempDir tempDir: Path) {
        val tempPath = tempDir.toAbsolutePath().toString()
        System.setProperty(SRC_DIR_KEY, tempPath)

        MakeFactoryCommand("dev.alpas.tests").main(arrayOf("Test", "--quiet"))

        val directory = File(tempPath, "database/factories")
        val testFactory = File(directory, "Test.kt")
        val comments = "// Add some comments"
        testFactory.appendText(comments)

        MakeFactoryCommand("dev.alpas.tests").main(arrayOf("TestFactory", "--quiet"))

        assertTrue(testFactory.readText().contains(comments))
    }

    @Test
    fun `can overwrite an existing factory by setting --force flag`(@TempDir tempDir: Path) {
        val tempPath = tempDir.toAbsolutePath().toString()
        System.setProperty(SRC_DIR_KEY, tempPath)

        MakeFactoryCommand("dev.alpas.tests").main(arrayOf("Test", "--quiet"))

        val directory = File(tempPath, "database/factories")
        val testFactory = File(directory, "TestFactory.kt")
        val comments = "// Add some comments"
        testFactory.appendText(comments)

        MakeFactoryCommand("dev.alpas.tests").main(arrayOf("Test", "--quiet", "--force"))

        assertFalse(testFactory.readText().contains(comments))
    }

    private val exampleStub = """
        package dev.alpas.tests.database.factories

        import dev.alpas.ozone.EntityFactory
        import dev.alpas.ozone.faker
        import dev.alpas.tests.entities.Test
        import dev.alpas.tests.entities.Tests

        internal class TestFactory : EntityFactory<Test> {
            override val table = Tests
            
            override fun entity(): Test {
                // https://alpas.dev/docs/ozone

                return Test {
                    // name = faker.name().name()
                    // updatedAt = faker.date().past(1, TimeUnit.HOURS).toInstant()
                    // createdAt = Instant.now()
                }
            }
        }
""".trimIndent()
}
