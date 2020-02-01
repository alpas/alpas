package dev.alpas.tests.commands

import dev.alpas.*
import dev.alpas.console.LinkWebCommand
import dev.alpas.tests.scope
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
class LinkWebTest {
    @Suppress("UNCHECKED_CAST")
    @Test
    fun `creates a linked web directory`(@TempDir tempDir: Path, @RelaxedMockK env: Environment, @RelaxedMockK config: AppConfig) {
        val rootPath = tempDir.toAbsolutePath().toString()

        val storagePath = Paths.get(rootPath, "storage").toAbsolutePath().toString()
        val webPath = Paths.get(storagePath, *RESOURCES_DIRS, "web").toAbsolutePath().toString()

        every { env.storagePath(*anyVararg()) } answers storagePath.scope()
        every { env.rootPath(*anyVararg()) } answers rootPath.scope()
        every { config getProperty "storageWebDirectory" } returns webPath

        File(storagePath).mkdir()

        System.setProperty(SRC_DIR_KEY, rootPath)
        LinkWebCommand(env, config).main(arrayOf("--quiet"))

        val link = File(webPath).toPath()
        assertTrue(Files.isSymbolicLink(link))
        val target = Files.readSymbolicLink(link)
        assertEquals(env.rootPath(*RESOURCES_DIRS, "web").toPath(), target)
    }
}
