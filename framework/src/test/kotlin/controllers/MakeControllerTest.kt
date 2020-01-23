package dev.alpas.tests.controllers

import dev.alpas.SRC_DIR_KEY
import dev.alpas.routing.console.MakeControllerCommand
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MakeControllerTest {
    @Test
    fun `can create one controller`(@TempDir tempDir: Path) {
        val tempPath = tempDir.toAbsolutePath().toString()
        System.setProperty(SRC_DIR_KEY, tempPath)
        MakeControllerCommand("dev.alpas.tests")
            .main(arrayOf("ExampleController", "--quiet"))

        val directory = File(tempPath, "controllers")
        assertTrue(directory.exists())
        val controller = File(directory, "ExampleController.kt")
        assertTrue(controller.exists())

        assertEquals(exampleStub, controller.readText())
    }

    @Test
    fun `can create multiple controllers`(@TempDir tempDir: Path) {
        val tempPath = tempDir.toAbsolutePath().toString()
        System.setProperty(SRC_DIR_KEY, tempPath)

        MakeControllerCommand("dev.alpas.tests")
            .main(arrayOf("ExampleController", "AnotherController", "--quiet"))

        val directory = File(tempPath, "controllers")
        val exampleController = File(directory, "ExampleController.kt")
        assertTrue(exampleController.exists())

        val anotherController = File(directory, "AnotherController.kt")
        assertTrue(anotherController.exists())

        assertEquals(exampleStub.replace("Example", "Another"), anotherController.readText())
    }

    @Test
    fun `doesnot overwrite an existing controller`(@TempDir tempDir: Path) {
        val tempPath = tempDir.toAbsolutePath().toString()
        System.setProperty(SRC_DIR_KEY, tempPath)

        MakeControllerCommand("dev.alpas.tests").main(arrayOf("ExampleController", "--quiet"))

        val directory = File(tempPath, "controllers")
        val exampleController = File(directory, "ExampleController.kt")
        val comments = "// Add some comments"
        exampleController.appendText(comments)

        MakeControllerCommand("dev.alpas.tests").main(arrayOf("ExampleController", "--quiet"))

        assertTrue(exampleController.readText().contains(comments))
    }

    @Test
    fun `can overwrite an existing controller by setting --force flag`(@TempDir tempDir: Path) {
        val tempPath = tempDir.toAbsolutePath().toString()
        System.setProperty(SRC_DIR_KEY, tempPath)

        MakeControllerCommand("dev.alpas.tests").main(arrayOf("ExampleController", "--quiet"))

        val directory = File(tempPath, "controllers")
        val exampleController = File(directory, "ExampleController.kt")
        val comments = "// Add some comments"
        exampleController.appendText(comments)

        MakeControllerCommand("dev.alpas.tests").main(arrayOf("ExampleController", "--quiet", "--force"))

        assertFalse(exampleController.readText().contains(comments))
    }
}

private val exampleStub = """
    package dev.alpas.tests.controllers

    import dev.alpas.http.HttpCall
    import dev.alpas.routing.Controller

    class ExampleController : Controller() {
        fun index(call: HttpCall) {
            call.reply("Hello, ExampleController!")
        }
    }
""".trimIndent()
