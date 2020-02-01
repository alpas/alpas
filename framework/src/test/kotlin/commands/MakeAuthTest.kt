package dev.alpas.tests.commands

import dev.alpas.SRC_DIR_KEY
import dev.alpas.auth.console.MakeAuthCommand
import dev.alpas.tests.CommandTestBase
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MakeAuthTest : CommandTestBase() {
    @Test
    fun `controller files are created`(@TempDir tempDir: Path) {
        val tempPath = tempDir.toAbsolutePath().toString()
        System.setProperty(SRC_DIR_KEY, tempPath)
        lastCommand = MakeAuthCommand("dev.alpas.tests").apply { main(arrayOf("--quiet")) }

        val controllersDir = File(tempPath, "controllers")
        assertTrue(controllersDir.exists())
        assertTrue(File(controllersDir, "HomeController.kt").exists())

        val authControllersDir = File(controllersDir, "auth")
        assertTrue(File(authControllersDir, "EmailVerificationController.kt").exists())
        assertTrue(File(authControllersDir, "ForgotPasswordController.kt").exists())
        assertTrue(File(authControllersDir, "LoginController.kt").exists())
        assertTrue(File(authControllersDir, "PasswordResetController.kt").exists())
        assertTrue(File(authControllersDir, "RegisterController.kt").exists())
    }

    @Test
    fun `resources templates are created`(@TempDir tempDir: Path) {
        val tempPath = tempDir.toAbsolutePath().toString()
        System.setProperty(SRC_DIR_KEY, tempPath)
        val command = MakeAuthCommand("dev.alpas.tests").apply {
            main(arrayOf("--quiet"))
            lastCommand = this
        }

        val resourcesDir = command.resourcesDir
        assertTrue(resourcesDir.exists())

        // resources/templates
        val templatesDir = File(resourcesDir, "templates")
        assertTrue(templatesDir.exists())
        assertTrue(File(templatesDir, "home.peb").exists())

        // resources/templates/auth
        val authDir = File(templatesDir, "auth")
        assertTrue(authDir.exists())
        assertTrue(File(authDir, "_header.peb").exists())
        assertTrue(File(authDir, "login.peb").exists())
        assertTrue(File(authDir, "register.peb").exists())
        assertTrue(File(authDir, "verify.peb").exists())

        // resources/templates/auth/emails
        val emailsDir = File(authDir, "emails")
        assertTrue(emailsDir.exists())
        assertTrue(File(emailsDir, "reset.peb").exists())
        assertTrue(File(emailsDir, "verify.peb").exists())

        // resources/templates/auth/passwords
        val passwordsDir = File(authDir, "passwords")
        assertTrue(passwordsDir.exists())
        assertTrue(File(passwordsDir, "reset_email.peb").exists())
        assertTrue(File(passwordsDir, "reset.peb").exists())

        // resources/templates/auth/layout
        val layoutDir = File(templatesDir, "layout")
        assertTrue(layoutDir.exists())
        assertTrue(File(layoutDir, "app.peb").exists())
    }
}
