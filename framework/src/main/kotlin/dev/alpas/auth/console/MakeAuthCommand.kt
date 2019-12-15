package dev.alpas.auth.console

import dev.alpas.auth.console.stubs.Stubs
import dev.alpas.console.GeneratorCommand
import dev.alpas.console.OutputFile
import java.io.File

class MakeAuthCommand(srcPackage: String) :
    GeneratorCommand(srcPackage, name = "make:auth", help = "Scaffold an authentication system.") {

    private val filesStubMap = mapOf(
        "auth/_header" to "view.header",
        "auth/login" to "view.login",
        "auth/register" to "view.register",
        "auth/verify" to "view.verify",
        "auth/emails/reset" to "view.email_reset",
        "auth/emails/verify" to "view.email_verify",
        "auth/passwords/reset_email" to "view.passwords_reset_email",
        "auth/passwords/reset" to "view.passwords_reset",
        "controllers/HomeController" to "controller.home",
        "controllers/auth/EmailVerificationController" to "controller.email_verification",
        "controllers/auth/LoginController" to "controller.login",
        "controllers/auth/ForgotPasswordController" to "controller.forgot_password",
        "controllers/auth/PasswordResetController" to "controller.password_reset",
        "controllers/auth/RegisterController" to "controller.register",
        "home" to "view.home",
        "layout/app" to "view.app_layout"
    )

    override val names = filesStubMap.keys.toList()

    override fun populateOutputFile(filename: String, actualname: String, vararg parentDirs: String): OutputFile {
        val target = if (filename.endsWith("Controller")) {
            File(sourceOutputPath(*parentDirs), "$filename.kt")
        } else {
            File(templatesOutputPath(*parentDirs), "$filename.peb")
        }
        return OutputFile()
            .target(target)
            .packageName(makePackageName(*parentDirs))
            .stub(Stubs.stubFor(filesStubMap[actualname]))
    }

    override fun onCompleted(outputFile: OutputFile) {
        withColors {
            echo(green("AUTHENTICATION SCAFFOLDING GENERATED 🙌"))
            echo("${yellow("Don't forget to call")} ${blue("authRoutes()")} ${yellow("from your routes file.")}")
        }
    }
}
