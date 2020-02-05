package dev.alpas.tests.commands

import dev.alpas.Environment
import dev.alpas.RESOURCES_DIRS
import dev.alpas.SRC_DIR_KEY
import dev.alpas.console.LinkTemplatesCommand
import dev.alpas.tests.scope
import dev.alpas.toPath
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class LinkTemplatesTest {
    @Suppress("UNCHECKED_CAST")
    @Test
    fun `creates a linked templates directory`(@TempDir tempDir: Path, @RelaxedMockK env: Environment) {
        val rootPath = tempDir.toAbsolutePath().toString()

        val storagePath = Paths.get(rootPath, "storage").toAbsolutePath().toString()
        val webPath = Paths.get(storagePath, *RESOURCES_DIRS, "templates").toAbsolutePath().toString()

        every { env.storagePath(*anyVararg()) } answers storagePath.scope()
        every { env.rootPath(*anyVararg()) } answers rootPath.scope()

        File(storagePath).mkdir()

        System.setProperty(SRC_DIR_KEY, rootPath)
        LinkTemplatesCommand(env).main(arrayOf("--quiet"))

        val link = File(webPath).toPath()
        assertTrue(Files.isSymbolicLink(link))
        val target = Files.readSymbolicLink(link)
        assertEquals(env.rootPath(*RESOURCES_DIRS, "templates").toPath(), target)
    }
}
