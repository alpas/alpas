package dev.alpas.tests

import dev.alpas.auth.console.MakeAuthCommand
import dev.alpas.console.Command
import org.junit.jupiter.api.AfterEach

abstract class ResourceCommandTestBase {
    protected var lastCommand: MakeAuthCommand? = null

    @AfterEach
    fun afterEach() {
        lastCommand?.deleteResources()
    }

    private fun Command.deleteResources() {
        resourcesDir.deleteRecursively()
    }
}
