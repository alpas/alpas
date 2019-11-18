package dev.alpas.auth

import dev.alpas.routing.Router

fun Router.authRoutes(
    middlewareGroup: List<String> = listOf("web"),
    supportRegistration: Boolean = true,
    supportPasswordReset: Boolean = true,
    supportEmailVerification: Boolean = true,
    packageName: String = "auth"
) {
    group {

        val loginController = "controllers.$packageName.LoginController"
        get("login", loginController, "showLoginForm").name("login").mustBeGuest()
        post("login", loginController, "login").mustBeGuest()
        post("logout", loginController, "logout").name("logout").mustBeAuthenticated()

        if (supportRegistration) {
            val registerController = "controllers.$packageName.RegisterController"
            get("register", registerController, "showRegistrationForm").name("register").mustBeGuest()
            post("register", registerController, "register").mustBeGuest()
        }

        if (supportPasswordReset) {
            group("password") {
                val forgotPasswordController = "controllers.$packageName.ForgotPasswordController"
                get("reset", forgotPasswordController, "showResetLinkRequestForm").name("request")
                post("email", forgotPasswordController, "sendResetLinkEmail").name("email")

                val resetPasswordController = "controllers.$packageName.PasswordResetController"
                get("reset/<token>", resetPasswordController, "showResetForm").name("reset")
                post("reset", resetPasswordController, "reset").name("update")
            }.name("password").mustBeGuest()
        }

        if (supportEmailVerification) {
            group("email") {
                val verificationController = "controllers.$packageName.EmailVerificationController"
                get("verify", verificationController, "showVerificationRequiredNotice").name("notice")
                get("verify/<id>", verificationController, "verify").name("verify").mustBeSigned()
                post("resend", verificationController, "resend").name("resend")
            }.name("verification").mustBeAuthenticated()
        }
    }.middlewareGroup(middlewareGroup)
}
