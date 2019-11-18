package dev.alpas.auth.console.stubs

internal class Stubs {
    companion object {
        fun stubFor(name: String?): String {
            return when (name?.toLowerCase()) {
                "view.header" -> headerViewStub()
                "view.login" -> loginViewStub()
                "view.register" -> registerViewStub()
                "view.verify" -> verifyViewStub()
                "view.email_reset" -> emailResetViewStub()
                "view.email_verify" -> emailVerifyViewStub()
                "view.passwords_reset_email" -> passwordResetEmailViewStub()
                "view.passwords_reset" -> passwordResetViewStub()
                "controller.home" -> homeControllerStub()
                "controller.email_verification" -> emailverificationcontrollerStub()
                "controller.login" -> loginControllerStub()
                "controller.forgot_password" -> forgotPasswordControllerStub()
                "controller.password_reset" -> passwordResetControllerStub()
                "controller.register" -> registerControllerStub()
                "view.home" -> homeViewStub()
                "view.app_layout" -> appLayoutViewStub()
                else -> ""
            }.trimIndent()
        }

        private fun headerViewStub(): String {
            return """
                <a href="/" class="text-5xl font-thin text-center my-10 flex justify-center items-center">
                    <svg enable-background="new 0 0 64 64" height="84" viewBox="0 0 64 64"
                         xmlns="http://www.w3.org/2000/svg">
                        <path d="m41.037 52.594c-2.644 1.901-5.769 2.906-9.037 2.906-5.649 0-10.867-3.117-13.577-8.02 2.679.552 5.622.879 8.559.984h.018c.269 0 .49-.212.5-.482.01-.275-.206-.508-.482-.518-9.203-.328-18.518-2.997-18.518-7.464 0-2.171 2.307-4.133 6.494-5.525.262-.087.403-.37.316-.633-.088-.261-.367-.406-.633-.316-4.695 1.561-7.177 3.8-7.177 6.474 0 3.48 4.118 5.859 9.657 7.197 2.748 5.658 8.541 9.303 14.843 9.303 3.479 0 6.806-1.07 9.621-3.094.224-.161.275-.474.114-.698-.162-.225-.474-.275-.698-.114z"/>
                        <path d="m16.02 44.125c.059.226.262.375.484.375.041 0 .083-.005.125-.016.267-.069.428-.342.359-.609-.324-1.258-.488-2.562-.488-3.875 0-1.622.253-3.186.717-4.657 4.499 1.408 9.595 2.157 14.783 2.157s10.284-.749 14.783-2.157c.464 1.471.717 3.035.717 4.657 0 .326-.011.649-.029.971-.017.275.194.512.47.528.265.014.513-.194.528-.47.02-.341.031-.683.031-1.029 0-1.898-.338-3.715-.931-5.413-.001-.005 0-.01-.002-.015-.002-.007-.008-.012-.01-.019-.643-1.83-1.585-3.519-2.791-4.991-.016-.021-.03-.044-.049-.063-3.029-3.662-7.605-6-12.718-6-5.112 0-9.688 2.338-12.717 6-.019.019-.033.042-.049.064-1.206 1.472-2.148 3.161-2.79 4.991-.003.007-.008.011-.01.018-.002.005 0 .01-.002.015-.593 1.698-.931 3.515-.931 5.413 0 1.397.175 2.785.52 4.125zm15.98-7.625c-5.071 0-10.046-.729-14.438-2.098.558-1.433 1.319-2.762 2.255-3.953 3.726 1.338 7.926 2.051 12.183 2.051s8.457-.713 12.183-2.051c.935 1.191 1.697 2.52 2.255 3.953-4.392 1.369-9.367 2.098-14.438 2.098zm0-12c4.557 0 8.649 1.989 11.487 5.131-7.066 2.43-15.909 2.43-22.975 0 2.839-3.142 6.931-5.131 11.488-5.131z"/>
                        <path d="m52.391 43.688c-.095-.119-.239-.188-.391-.188h-14.5v-1.5c0-.204-.124-.389-.314-.464l-5-2c-.153-.063-.328-.043-.466.05s-.22.248-.22.414v5.5h-2.5c-.276 0-.5.224-.5.5v2c0 .276.224.5.5.5h2.5v.5c0 .276.224.5.5.5h7.592c.207.581.757 1 1.408 1h8c.651 0 1.201-.419 1.408-1h5.592c.192 0 .367-.11.45-.283.084-.174.061-.379-.06-.529zm-22.891 3.812v-1h2v1zm19.5 2h-8c-.275 0-.5-.225-.5-.5s.225-.5.5-.5h8c.275 0 .5.225.5.5s-.225.5-.5.5zm1.408-1c-.207-.581-.757-1-1.408-1h-8c-.651 0-1.201.419-1.408 1h-7.092v-.5-2-5.262l4 1.601v1.661c0 .276.224.5.5.5h14.76l3.2 4z"/>
                        <path d="m50 45.5h-13c-.276 0-.5.224-.5.5s.224.5.5.5h13c.276 0 .5-.224.5-.5s-.224-.5-.5-.5z"/>
                        <path d="m48 24.5c4.687 0 8.5-3.813 8.5-8.5s-3.813-8.5-8.5-8.5-8.5 3.813-8.5 8.5 3.813 8.5 8.5 8.5zm0-16c4.136 0 7.5 3.364 7.5 7.5s-3.364 7.5-7.5 7.5-7.5-3.364-7.5-7.5 3.364-7.5 7.5-7.5z"/>
                        <path d="m45 16.5c1.379 0 2.5-1.121 2.5-2.5s-1.121-2.5-2.5-2.5-2.5 1.121-2.5 2.5 1.121 2.5 2.5 2.5zm0-4c.827 0 1.5.673 1.5 1.5s-.673 1.5-1.5 1.5-1.5-.673-1.5-1.5.673-1.5 1.5-1.5z"/>
                        <circle cx="51" cy="19" r="1"/>
                        <circle cx="54" cy="35" r="1"/>
                        <circle cx="23" cy="16" r="1"/>
                    </svg>
                    <div class="ml-2"> Alpas</div>
                </a>

                {% if hasFlash('success') %}
                    <div class="bg-green-100 text-green-900 mb-2 p-4 text-lg">{{ flash('success') }}</div>
                {% endif %}
            """.trimIndent()
        }

        private fun loginViewStub(): String {
            return """
                {% extends "../layout/app.twig" %}
                {% block content %}

                    <div class="h-full mt-10 pt-10">
                        <div class="w-full max-w-md mx-auto">
                            {% include "./_header.twig" %}
                            <form class="bg-white p-8 mx-auto bg-white shadow-md rounded mb-4 {{ errors.isEmpty() ? '' : 'border-red-500 border' }}"
                                  method="POST" action="{{ route('login') }}">
                                {% csrf %}
                                <div class="pb-8 text-indigo-600">
                                    <h1 class="text-2xl text-center font-normal">Hi!</h1>
                                    <h2 class="text-xl text-center font-normal">It's good to see you again :)</h2>
                                    <h4 class="text-sm text-center font-normal text-gray-700 mt-2">
                                        Log in by entering your information below
                                    </h4>
                                </div>
                                <div>
                                    <label class="block text-gray-700 font-bold mb-2" for="email"> Email Address </label>
                                    <input class="text-lg shadow appearance-none border rounded w-full py-3 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline {{ whenError('email', 'border-red-500') }}"
                                           id="email" type="text" name="email" value="{{ old('email') }}" required autofocus>
                                    {% if hasError('email') %}
                                        <p class="text-red-600 text-sm mt-2">{{ firstError('email') }}</p>
                                    {% endif %}
                                </div>
                                <div class="mt-6">
                                    <label class="block text-gray-700 font-bold mb-2" for="password"> Password </label>
                                    <input class="text-lg shadow appearance-none border rounded w-full py-3 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline {{ whenError('password', 'border-red-500') }}"
                                           id="password" type="password" name="password" required>
                                    {% if hasError('password') %}
                                        <p class="text-red-600 text-sm mt-2">{{ firstError('password') }}</p>
                                    {% endif %}
                                </div>
                                <div class="flex items-center justify-between mt-10">
                                    <button class="bg-blue-500 hover:bg-blue-700 text-white font-bold py-3 px-4 rounded focus:outline-none focus:shadow-outline"
                                            type="submit">
                                        Log In
                                    </button>

                                    {% if hasRoute('password.request') %}
                                        <div class="inline-block align-baseline text-sm">
                                            Forgot Password?
                                            <a href="{{ route('password.request') }}"
                                               class="text-blue-500 hover:text-blue-800 font-bold"> Reset </a>
                                        </div>
                                    {% endif %}
                                </div>
                                {% if hasRoute('register') %}
                                    <div class="text-sm mt-6">
                                        Need an account?
                                        <a class="text-blue-500 hover:text-blue-800 font-bold"
                                           href="{{ route('register') }}">Register</a>
                                    </div>
                                {% endif %}
                            </form>
                        </div>
                    </div>

                {% endblock %}
            """.trimIndent()
        }

        private fun registerViewStub(): String {
            return """
                {% extends "../layout/app.twig" %}
                {% block content %}

                    <div class="h-full mt-10 pt-10">
                        <div class="w-full max-w-md mx-auto">
                            {% include "./_header.twig" %}
                            <form class="bg-white p-8 mx-auto bg-white shadow-md rounded mb-4 {{ errors.isEmpty() ? '' : 'border-red-500 border' }}"
                                  method="POST" action="{{ route('register') }}">
                                {% csrf %}
                                <div class="pb-8 text-indigo-600">
                                    <h1 class="text-2xl text-center font-normal">Hello!</h1>
                                    <h2 class="text-xl text-center font-normal">It's nice to meet you :)</h2>
                                    <h4 class="text-sm text-center font-normal text-gray-700 mt-2">
                                        Sign up by entering your information below
                                    </h4>
                                </div>
                                <div class="mt-6">
                                    <label class="block text-gray-700 font-bold mb-2" for="name"> Name </label>
                                    <input class="text-lg shadow appearance-none border rounded w-full py-3 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline {{ whenError('name', 'border-red-500') }}"
                                           id="name" type="text" name="name" value="{{ old('name') }}" required>
                                    {% if hasError('name') %}
                                        <p class="text-red-600 text-sm mt-2">{{ firstError('name') }}</p>
                                    {% endif %}
                                </div>
                                <div class="mt-6">
                                    <label class="block text-gray-700 font-bold mb-2" for="email"> Email Address </label>
                                    <input class="text-lg shadow appearance-none border rounded w-full py-3 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline {{ whenError('email', 'border-red-500') }}"
                                           id="email" type="text" name="email" value="{{ old('email') }}" required>
                                    {% if hasError('email') %}
                                        <p class="text-red-600 text-sm mt-2">{{ firstError('email') }}</p>
                                    {% endif %}
                                </div>
                                <div class="mt-6">
                                    <label class="block text-gray-700 font-bold mb-2" for="password"> Password </label>
                                    <input class="text-lg shadow appearance-none border rounded w-full py-3 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline {{ whenError('password', 'border-red-500') }}"
                                           id="password" type="password" name="password" required>
                                    {% if hasError('password') %}
                                        <p class="text-red-600 text-sm mt-2">{{ firstError('password') }}</p>
                                    {% endif %}
                                </div>
                                <div class="mt-6">
                                    <label class="block text-gray-700 font-bold mb-2" for="confirm-password"> Confirm Password </label>
                                    <input class="text-lg shadow appearance-none border rounded w-full py-3 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline {{ whenError('confirm_password', 'border-red-500') }}"
                                           id="confirm-password" type="password" name="confirm_password" required>
                                    {% if hasError('password') %}
                                        <p class="text-red-600 text-sm mt-2">{{ firstError('confirm_password') }}</p>
                                    {% endif %}
                                </div>
                                <div class="mt-10">
                                    <button class="bg-blue-500 hover:bg-blue-700 text-white font-bold py-3 px-4 rounded focus:outline-none focus:shadow-outline"
                                            type="submit">
                                        Register
                                    </button>

                                </div>
                                <div class="text-sm mt-6">
                                    Already have an account?
                                    <a class="text-blue-500 hover:text-blue-800 font-bold" href="{{ route('login') }}">Log in</a>
                                </div>
                            </form>
                        </div>
                    </div>

                {% endblock %}

            """.trimIndent()
        }

        private fun verifyViewStub(): String {
            return """
                {% extends "../layout/app.twig" %}
                {% block content %}

                    <div class="h-full mt-10 pt-10">
                        <div class="w-full max-w-md mx-auto">
                            {% include "./_header.twig" %}
                            <div class="bg-white p-8 mx-auto bg-white shadow-md rounded mb-4">
                                <div class="pb-8">
                                    <div class="text-blue-500 my-12">
                                        <svg aria-hidden="true" focusable="false" class="mx-auto" role="img" height="46"
                                             xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512">
                                            <path fill="currentColor"
                                                  d="M464 4.3L16 262.7C-7 276-4.7 309.9 19.8 320L160 378v102c0 30.2 37.8 43.3 56.7 20.3l60.7-73.8 126.4 52.2c19.1 7.9 40.7-4.2 43.8-24.7l64-417.1C515.7 10.2 487-9 464 4.3zM192 480v-88.8l54.5 22.5L192 480zm224-30.9l-206.2-85.2 199.5-235.8c4.8-5.6-2.9-13.2-8.5-8.4L145.5 337.3 32 290.5 480 32l-64 417.1z"></path>
                                        </svg>
                                    </div>
                                    <h1 class="text-xl text-center font-normal text-gray-800">Please Verify Your Email</h1>
                                    <p class="text-sm text-center font-normal text-gray-700 text-lg my-8">
                                        We've sent an email to <span class="font-bold">{{ auth.user.email }} </span> with a link to
                                        verify your account.
                                    </p>

                                    <p class="text-sm text-center font-normal text-gray-700 text-lg my-8">
                                        Please follow the link in the email to complete the registration.
                                    </p>

                                    <form action="{{ route('verification.resend') }}" method="post" class="text-center">
                                        {% csrf %}
                                        <button class="hover:text-blue-700 mt-12 text-blue-500 text-lg" type="submit">
                                            Resend verification email
                                        </button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>

                {% endblock %}
            """.trimIndent()
        }

        private fun emailResetViewStub(): String {
            return """
                <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
                <html xmlns="http://www.w3.org/1999/xhtml">

                <head>
                    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                    <meta content="telephone=no" name="format-detection"/>
                    <style type="text/css" data-premailer="ignore">
                        @import url(https://fonts.googleapis.com/css?family=Open+Sans:300,400,500,600,700);
                    </style>

                    <style>
                        body {
                            margin: 0;
                            padding: 0;
                            background-color: #edf2f7;
                            font-size: 15px;
                            line-height: 160%;
                            mso-line-height-rule: exactly;
                            color: #444444;
                            width: 100%;
                        }

                        body {
                            font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif;
                        }

                        a:hover {
                            text-decoration: underline;
                        }

                        .btn:hover {
                            text-decoration: none;
                        }

                        a.bg-green:hover {
                            background-color: #56ab00 !important;
                        }

                    </style>
                </head>

                <body style="font-size: 15px; margin: 0; padding: 0; line-height: 23px; mso-line-height-rule: exactly; color: #444444; width: 100%; font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif;"
                      bgcolor="#f5f7fb">
                <table width="100%" cellspacing="0" cellpadding="0" bgcolor="#f5f7fb"
                       style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%; -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%;">
                    <tr>
                        <td align="center" valign="top"
                            style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif;">
                            <table class="wrap" cellspacing="0" cellpadding="0"
                                   style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%; max-width: 640px; text-align: left;">
                                <tr>
                                    <td class="p-sm"
                                        style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding: 8px;">
                                        <table cellpadding="0" cellspacing="0"
                                               style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%;">
                                            <tr>
                                                <td class="py-lg"
                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding-top: 24px; padding-bottom: 24px;">
                                                    <table cellspacing="0" cellpadding="0"
                                                           style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%;">
                                                        <tr>
                                                            <td style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif;">
                                                                <h3 style="font-size: 28px; font-weight: 300; color: black;"> {{ env("APP_NAME") }} </h3>
                                                            </td>
                                                        </tr>
                                                    </table>
                                                </td>
                                            </tr>
                                        </table>
                                        <div>
                                            <table class="box" cellpadding="0" cellspacing="0" bgcolor="#ffffff"
                                                   style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%; border-radius: 3px; -webkit-box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05); box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05); border: 1px solid #f0f0f0;">
                                                <tr>
                                                    <td style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif;">
                                                        <table cellpadding="0" cellspacing="0"
                                                               style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%;">
                                                            <tr>
                                                                <td class="content pb-0" align="center"
                                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding: 40px 48px 0;">
                                                                    <h1 class="text-center m-0 mt-md"
                                                                        style="font-size: 28px; color: #555; font-weight: 500; line-height: 130%; margin: 26px 0 0;"
                                                                        align="center">
                                                                        Reset Password</h1>
                                                                </td>
                                                            </tr>
                                                            <tr>
                                                                <td class="content"
                                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding: 20px 48px;">
                                                                    <p> You recently requested to reset the password for
                                                                        your {{ env("APP_NAME") }} account. Please use the button below
                                                                        to reset your password. Please not that the reset link will
                                                                        expire in about {{ tokenExpirationDuration }}
                                                                        hours.</p>
                                                                </td>
                                                            </tr>

                                                            <tr>
                                                                <td align="center"
                                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding: 40px 0px">
                                                                    <table cellpadding="0" cellspacing="0" border="0" bgcolor="#5EBA00"
                                                                           class="bg-green rounded w-auto"
                                                                           style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: separate; width: auto; color: #ffffff; border-radius: 3px;">
                                                                        <tbody>
                                                                        <tr>
                                                                            <td align="center" valign="top" class="lh-1"
                                                                                style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; line-height: 100%;">
                                                                                <a href="{{ resetUrl }}"
                                                                                   class="btn bg-green border-blue"
                                                                                   style="color: #ffffff; padding: 12px 32px; border: 1px solid #5EBA00; text-decoration: none; white-space: nowrap; font-weight: 600; font-size: 16px; border-radius: 3px; line-height: 100%; display: block; -webkit-transition: .3s background-color; transition: .3s background-color; background-color: #5EBA00;">
                                                                                        <span class="btn-span"
                                                                                              style="color: #ffffff; font-size: 16px; text-decoration: none; white-space: nowrap; font-weight: 600; line-height: 100%;">Reset Password</span>
                                                                                </a>
                                                                            </td>
                                                                        </tr>
                                                                        </tbody>
                                                                    </table>
                                                                </td>
                                                            </tr>
                                                            <tr>
                                                                <td class="content"
                                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding: 10PX 48px;">
                                                                    <p>If you received this email by mistake, you can simply ignore
                                                                        it. </p>
                                                                </td>
                                                            </tr>
                                                            <tr>
                                                                <td class="content pt-0"
                                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding: 0px 48px;">
                                                                    <table cellspacing="0" cellpadding="0"
                                                                           style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%;">
                                                                        <tr>
                                                                            <td class="va-middle"
                                                                                style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif;"
                                                                                valign="middle"> Thank you! <br/>
                                                                                <span class="text-muted"
                                                                                      style="color: #666;">Team {{ env("APP_NAME") }}</span>
                                                                            </td>
                                                                        </tr>
                                                                    </table>
                                                                </td>

                                                            </tr>

                                                            <tr>
                                                                <td class="content text-muted pt-0 text-center font-sm"
                                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; color: #9eb0b7; font-size: 13px; padding: 20px 48px; margin-top: 20px"
                                                                    align="center"> If you are having trouble with the button above?
                                                                    Please <a href="{{ resetUrl }}">copy
                                                                        this URL</a> and paste it into your browser.
                                                                </td>
                                                            </tr>
                                                        </table>
                                                    </td>
                                                </tr>
                                            </table>
                                        </div>
                                        <table cellspacing="0" cellpadding="0"
                                               style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%;">
                                            <tr>
                                                <td class="py-xl"
                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding: 40px 48px">
                                                    <table class="font-sm text-center text-muted" cellspacing="0" cellpadding="0"
                                                           style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%; color: #9eb0b7; text-align: center; font-size: 13px;">
                                                        <tr>
                                                            <td align="center" class="pb-md"
                                                                style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding-bottom: 16px;">

                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td class="px-lg"
                                                                style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding-right: 24px; padding-left: 24px;">
                                                                Copyright © 2019 {{ env("APP_NAME") }}, All rights reserved.
                                                            </td>
                                                        </tr>
                                                    </table>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
                </body>
                </html>
            """.trimIndent()
        }

        private fun emailVerifyViewStub(): String {
            return """
                <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
                <html xmlns="http://www.w3.org/1999/xhtml">

                <head>
                    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                    <meta content="telephone=no" name="format-detection"/>
                    <style type="text/css" data-premailer="ignore">
                        @import url(https://fonts.googleapis.com/css?family=Open+Sans:300,400,500,600,700);
                    </style>

                    <style>
                        body {
                            margin: 0;
                            padding: 0;
                            background-color: #edf2f7;
                            font-size: 15px;
                            line-height: 160%;
                            mso-line-height-rule: exactly;
                            color: #444444;
                            width: 100%;
                        }

                        body {
                            font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif;
                        }

                        a:hover {
                            text-decoration: underline;
                        }

                        .btn:hover {
                            text-decoration: none;
                        }

                        a.bg-green:hover {
                            background-color: #56ab00 !important;
                        }

                    </style>
                </head>

                <body style="font-size: 15px; margin: 0; padding: 0; line-height: 23px; mso-line-height-rule: exactly; color: #444444; width: 100%; font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif;"
                      bgcolor="#f5f7fb">
                <table width="100%" cellspacing="0" cellpadding="0" bgcolor="#f5f7fb"
                       style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%; -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%;">
                    <tr>
                        <td align="center" valign="top"
                            style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif;">
                            <table class="wrap" cellspacing="0" cellpadding="0"
                                   style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%; max-width: 640px; text-align: left;">
                                <tr>
                                    <td class="p-sm"
                                        style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding: 8px;">
                                        <table cellpadding="0" cellspacing="0"
                                               style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%;">
                                            <tr>
                                                <td class="py-lg"
                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding-top: 24px; padding-bottom: 24px;">
                                                    <table cellspacing="0" cellpadding="0"
                                                           style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%;">
                                                        <tr>
                                                            <td style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif;">
                                                                <h3 style="font-size: 28px; font-weight: 300; color: black;"> {{ env("APP_NAME") }} </h3>
                                                            </td>
                                                        </tr>
                                                    </table>
                                                </td>
                                            </tr>
                                        </table>
                                        <div>
                                            <table class="box" cellpadding="0" cellspacing="0" bgcolor="#ffffff"
                                                   style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%; border-radius: 3px; -webkit-box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05); box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05); border: 1px solid #f0f0f0;">
                                                <tr>
                                                    <td style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif;">
                                                        <table cellpadding="0" cellspacing="0"
                                                               style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%;">
                                                            <tr>
                                                                <td class="content pb-0" align="center"
                                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding: 40px 48px 0;">
                                                                    <h1 class="text-center m-0 mt-md"
                                                                        style="font-size: 28px; color: #555; font-weight: 500; line-height: 130%; margin: 26px 0 0;"
                                                                        align="center">
                                                                        Thank you for registering, {{ username }}! </h1>
                                                                </td>
                                                            </tr>
                                                            <tr>
                                                                <td class="content"
                                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding: 20px 48px;">
                                                                    <p> There's only one step left before you can start
                                                                        using {{ env("APP_NAME") }}. Please verify your email address by
                                                                        clicking the button below.</p>
                                                                </td>
                                                            </tr>

                                                            <tr>
                                                                <td align="center"
                                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding: 40px 0px">
                                                                    <table cellpadding="0" cellspacing="0" border="0"
                                                                           class="bg-green rounded w-auto"
                                                                           style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: separate; width: auto; color: #ffffff; border-radius: 3px;"
                                                                           bgcolor="#5EBA00">
                                                                        <tbody>
                                                                        <tr>
                                                                            <td align="center" valign="top" class="lh-1"
                                                                                style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; line-height: 100%;">
                                                                                <a href="{{ verificationUrl }}"
                                                                                   class="btn bg-green border-blue"
                                                                                   style="color: #ffffff; padding: 12px 32px; border: 1px solid #5EBA00; text-decoration: none; white-space: nowrap; font-weight: 600; font-size: 16px; border-radius: 3px; line-height: 100%; display: block; -webkit-transition: .3s background-color; transition: .3s background-color; background-color: #5EBA00;">
                                                                                        <span class="btn-span"
                                                                                              style="color: #ffffff; font-size: 16px; text-decoration: none; white-space: nowrap; font-weight: 600; line-height: 100%;">Confirm email address</span>
                                                                                </a>
                                                                            </td>
                                                                        </tr>
                                                                        </tbody>
                                                                    </table>
                                                                </td>
                                                            </tr>
                                                            <tr>
                                                                <td class="content"
                                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding: 10PX 48px;">
                                                                    <p>If you received this email by mistake, you can simply ignore
                                                                        it. </p>
                                                                </td>
                                                            </tr>
                                                            <tr>
                                                                <td class="content pt-0"
                                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding: 0px 48px;">
                                                                    <table cellspacing="0" cellpadding="0"
                                                                           style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%;">
                                                                        <tr>
                                                                            <td class="va-middle"
                                                                                style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif;"
                                                                                valign="middle"> Thank you! <br/>
                                                                                <span class="text-muted"
                                                                                      style="color: #666;">Team {{ env("APP_NAME") }}</span>
                                                                            </td>
                                                                        </tr>
                                                                    </table>
                                                                </td>

                                                            </tr>

                                                            <tr>
                                                                <td class="content text-muted pt-0 text-center font-sm"
                                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; color: #9eb0b7; font-size: 13px; padding: 20px 48px; margin-top: 20px"
                                                                    align="center"> If you are having trouble with the button above?
                                                                    Please <a href="{{ verificationUrl }}">copy this URL</a> and paste
                                                                    it into your browser.
                                                                </td>
                                                            </tr>
                                                        </table>
                                                    </td>
                                                </tr>
                                            </table>
                                        </div>
                                        <table cellspacing="0" cellpadding="0"
                                               style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%;">
                                            <tr>
                                                <td class="py-xl"
                                                    style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding: 40px 48px">
                                                    <table class="font-sm text-center text-muted" cellspacing="0" cellpadding="0"
                                                           style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; border-collapse: collapse; width: 100%; color: #9eb0b7; text-align: center; font-size: 13px;">
                                                        <tr>
                                                            <td align="center" class="pb-md"
                                                                style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding-bottom: 16px;">

                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td class="px-lg"
                                                                style="font-family: Open Sans, -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, Helvetica, Arial, sans-serif; padding-right: 24px; padding-left: 24px;">
                                                                Copyright © 2019 {{ env("APP_NAME") }}, All rights reserved.
                                                            </td>
                                                        </tr>
                                                    </table>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
                </body>
                </html>
            """.trimIndent()
        }

        private fun passwordResetEmailViewStub(): String {
            return """
                {% extends "../../layout/app.twig" %}
                {% block content %}

                    <div class="h-full mt-10 pt-10">
                        <div class="w-full max-w-md mx-auto">
                            {% include "../_header.twig" %}
                            <form class="bg-white p-8 mx-auto bg-white shadow-md rounded mb-4 {{ errors.isEmpty() ? '' : 'border-red-500 border' }}"
                                  method="POST" action="{{ route('password.email') }}">
                                {% csrf %}
                                <div class="pb-8 text-indigo-600">
                                    <h1 class="text-2xl text-center font-normal">Hi!</h1>
                                    <h2 class="text-xl text-center font-normal">We are here to help :)</h2>
                                    <h4 class="text-sm text-center font-normal text-gray-700 mt-2">
                                        Enter your email address to reset your password
                                    </h4>
                                </div>
                                <div>
                                    <label class="block text-gray-700 font-bold mb-2" for="email"> Email Address </label>
                                    <input class="text-lg shadow appearance-none border rounded w-full py-3 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline {{ whenError('email', 'border-red-500') }}"
                                           id="email" type="text" name="email" value="{{ old('email') }}" required autofocus>
                                    {% if hasError('email') %}
                                        <p class="text-red-600 text-sm mt-2">{{ firstError('email') }}</p>
                                    {% endif %}
                                </div>
                                <div class="mt-10">
                                    <button class="bg-blue-500 hover:bg-blue-700 text-white font-bold py-3 px-4 rounded focus:outline-none focus:shadow-outline"
                                            type="submit">
                                        Send Password Reset Link
                                    </button>
                                </div>
                                <div class="text-sm mt-6">
                                    <a class="text-blue-500 hover:text-blue-800 font-bold" href="{{ route('login') }}">
                                        &leftarrow; Back to Log in
                                    </a>
                                </div>
                            </form>
                        </div>
                    </div>

                {% endblock %}
            """.trimIndent()
        }

        private fun passwordResetViewStub(): String {
            return """
                {% extends "../../layout/app.twig" %}
                {% block content %}

                    <div class="h-full mt-10 pt-10">
                        <div class="w-full max-w-md mx-auto">
                            {% include "../_header.twig" %}
                            <form class="bg-white p-8 mx-auto bg-white shadow-md rounded mb-4 {{ errors.isEmpty() ? '' : 'border-red-500 border' }}"
                                  method="POST" action="{{ route('password.update') }}">
                                {% csrf %}
                                <input type="hidden" name="token" value="{{ token }}">
                                <div class="pb-8 text-indigo-600">
                                    <h1 class="text-2xl text-center font-normal">Welcome back!</h1>
                                    <h2 class="text-xl text-center font-normal">Happy to see you :)</h2>
                                    <h4 class="text-sm text-center font-normal text-gray-700 mt-2">
                                        To continue, please set a strong password.
                                    </h4>
                                </div>
                                <div class="mt-6">
                                    <label class="block text-gray-700 font-bold mb-2" for="email"> Email Address </label>
                                    <input class="text-lg shadow appearance-none border rounded w-full py-3 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline {{ whenError('email', 'border-red-500') }}"
                                           id="email" type="text" name="email" value="{{ old('email') }}" required>
                                    {% if hasError('email') %}
                                        <p class="text-red-600 text-sm mt-2">{{ firstError('email') }}</p>
                                    {% endif %}
                                </div>
                                <div class="mt-6">
                                    <label class="block text-gray-700 font-bold mb-2" for="password"> Password </label>
                                    <input class="text-lg shadow appearance-none border rounded w-full py-3 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline {{ whenError('password', 'border-red-500') }}"
                                           id="password" type="password" name="password" required>
                                    {% if hasError('password') %}
                                        <p class="text-red-600 text-sm mt-2">{{ firstError('password') }}</p>
                                    {% endif %}
                                </div>
                                <div class="mt-6">
                                    <label class="block text-gray-700 font-bold mb-2" for="confirm-password"> Confirm Password </label>
                                    <input class="text-lg shadow appearance-none border rounded w-full py-3 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline {{ whenError('confirm_password', 'border-red-500') }}"
                                           id="confirm-password" type="password" name="confirm_password" required>
                                    {% if hasError('password') %}
                                        <p class="text-red-600 text-sm mt-2">{{ firstError('confirm_password') }}</p>
                                    {% endif %}
                                </div>
                                <div class="mt-10">
                                    <button class="bg-blue-500 hover:bg-blue-700 text-white font-bold py-3 px-4 rounded focus:outline-none focus:shadow-outline"
                                            type="submit">
                                        Continue
                                    </button>

                                </div>
                                <div class="text-sm mt-6">
                                    <a class="text-blue-500 hover:text-blue-800 font-bold" href="{{ route('login') }}">
                                        &leftarrow; Back to Log in
                                    </a>
                                </div>
                            </form>
                        </div>
                    </div>

                {% endblock %}
            """.trimIndent()
        }

        private fun homeControllerStub(): String {
            return """
                package StubPackageName

                import VerifiedEmailOnlyMiddleware
                import HttpCall
                import Controller
                import ControllerMiddleware

                class StubClazzName : Controller() {
                    override fun middleware(call: HttpCall) = listOf(ControllerMiddleware(VerifiedEmailOnlyMiddleware::class))

                    fun index(call: HttpCall) {
                        call.render("home")
                    }
                }
            """.trimIndent()
        }

        private fun emailverificationcontrollerStub(): String {
            return """
                package StubPackageName

                import HandlesEmailVerification
                import Controller

                class StubClazzName : Controller(), HandlesEmailVerification
            """.trimIndent()
        }

        private fun forgotPasswordControllerStub(): String {
            return """
                package StubPackageName

                import HandlesForgottenPassword
                import Controller

                class StubClazzName : Controller(), HandlesForgottenPassword
            """.trimIndent()
        }

        private fun loginControllerStub(): String {
            return """
                package StubPackageName

                import HandlesUserLogin
                import Controller

                class StubClazzName : Controller(), HandlesUserLogin
            """.trimIndent()
        }

        private fun passwordResetControllerStub(): String {
            return """
                package StubPackageName

                import HandlesPasswordReset
                import Controller

                class StubClazzName : Controller(), HandlesPasswordReset
            """.trimIndent()
        }

        private fun registerControllerStub(): String {
            return """
                package StubPackageName

                import HandlesUserRegistration
                import Controller

                class StubClazzName : Controller(), HandlesUserRegistration
            """.trimIndent()
        }

        private fun homeViewStub(): String {
            return """
                {% extends "layout/app.twig" %}
                {% block content %}

                    <div class="bg-white h-full max-w-4xl mt-10 mx-auto p-10 pt-10 w-full">
                        <h3 class="font-medium mb-3 text-2xl">Welcome, {{ auth.user.name }}! </h3>
                        This is your
                        <pre class="bg-gray-200 inline-flex px-1">resources/templates/home</pre>
                        page.
                        <p class="text-gray-800 mt-4">Lorem ipsum dolor sit amet, consectetur adipisicing elit. Accusamus assumenda
                            dicta
                            incidunt molestiae,
                            nesciunt officiis quas quos. Autem consequuntur corporis, dolore eos inventore magnam mollitia numquam
                            pariatur, porro, similique temporibus! Lorem ipsum dolor sit amet, consectetur adipisicing elit. Adipisci
                            aliquam asperiores aut, dolorem eum, illo in natus nesciunt nihil optio porro praesentium provident quaerat,
                            quidem sint totam veniam vero vitae. Lorem ipsum dolor sit amet, consectetur adipisicing elit. A atque
                            deleniti facilis fugiat harum impedit magni minus molestiae perspiciatis, provident quisquam, quos rem sint
                            suscipit tenetur totam vel veniam vitae.</p>

                        <div class="flex font-medium justify-around links mt-10 mt-lg mx-16 uppercase">
                            <a href="https://alpas.dev/"><span class="text-gray-600">/</span> Alpas</a>
                            <a href="https://alpas.dev/docs"><span class="text-gray-600">/</span> Docs</a>
                            <a href="https://getcleaver.com/"><span class="text-gray-600">/</span> Cleaver</a>
                            <a href="https://github.com/alpas/alpas"><span class="text-gray-600">/</span> GitHub</a>
                        </div>
                    </div>
                    
                {% endblock %}
            """.trimIndent()
        }

        private fun appLayoutViewStub(): String {
            return """
                <!doctype html>
                <html lang="en">

                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
                    <link rel="stylesheet" href="{{ mix('css/app.css') }}">
                    <title>🚀 Alpas</title>
                    <style>
                        #menu-toggle:checked + #menu {
                            display: block;
                        }
                    </style>
                </head>

                <body class="antialiased bg-gray-200 h-full">

                {% if not routeIsOneOf(['login', 'register', 'password.request', 'password.reset']) %}
                    <header class="lg:px-16 px-6 bg-white flex flex-wrap items-center py-3">
                        <div class="flex-1 flex justify-between items-center">
                            <a href="/" class="text-xl font-medium"> 🚀 Alpas</a>
                        </div>

                        <label for="menu-toggle" class="pointer-cursor lg:hidden block">
                            <svg class="fill-current text-gray-900" xmlns="http://www.w3.org/2000/svg" width="20" height="20"
                                 viewBox="0 0 20 20"><title>menu</title>
                                <path d="M0 3h20v2H0V3zm0 6h20v2H0V9zm0 6h20v2H0v-2z"></path>
                            </svg>
                        </label>
                        <input class="hidden" type="checkbox" id="menu-toggle"/>

                        <div class="hidden lg:flex lg:items-center lg:w-auto w-full" id="menu">
                            <div class="flex flex-col-reverse lg:flex lg:flex-row lg:w-auto w-full mt-6 lg:mt-0">
                                {% guest %}
                                    <nav>
                                        <ul class="lg:flex items-center justify-between text-base text-gray-700 pt-4 lg:pt-0">
                                            <li>
                                                <a class="lg:p-4 py-3 px-0 block border-b-2 border-transparent hover:border-indigo-400"
                                                   href="{{ route('login') }}">
                                                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"
                                                         fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                                                         stroke-linejoin="round" class="inline">
                                                        <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"></path>
                                                        <polyline points="10 17 15 12 10 7"></polyline>
                                                        <line x1="15" y1="12" x2="3" y2="12"></line>
                                                    </svg>
                                                    Log in
                                                </a>
                                            </li>
                                            <li>
                                                <a class="lg:p-4 py-3 px-0 block border-b-2 border-transparent hover:border-indigo-400"
                                                   href="{{ route('register') }}">
                                                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"
                                                         fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                                                         stroke-linejoin="round" class="inline">
                                                        <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                                                        <circle cx="8.5" cy="7" r="4"></circle>
                                                        <line x1="20" y1="8" x2="20" y2="14"></line>
                                                        <line x1="23" y1="11" x2="17" y2="11"></line>
                                                    </svg>
                                                    Register
                                                </a>
                                            </li>
                                        </ul>
                                    </nav>
                                    {% else %}
                                    <form action="{{ route('logout') }}" method="post">
                                        {% csrf %}
                                        <button class="bg-transparent hover:bg-blue-500 text-blue-700 font-semibold hover:text-white py-2 px-4 border border-blue-500 hover:border-transparent rounded mr-4"
                                                type="submit">
                                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"
                                                 fill="none"
                                                 stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
                                                 class="inline mr-1">
                                                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                                                <polyline points="16 17 21 12 16 7"></polyline>
                                                <line x1="21" y1="12" x2="9" y2="12"></line>
                                            </svg>
                                            Log Out
                                        </button>
                                    </form>
                                {% endguest %}
                                {% auth %}
                                    <div class="lg:ml-4 flex items-center justify-start lg:mb-0 mb-4 pointer-cursor border-b lg:border-0 pb-4 lg:pb-0">
                                        <img class="rounded-full w-10 h-10 border-2 border-gray-300"
                                             src="//www.gravatar.com/avatar/?d=robohash" alt="Alpas">
                                        <span class="ml-3 text-gray-700">{{ auth.user.email }}</span>
                                    </div>
                                {% endauth %}
                            </div>
                        </div>
                    </header>
                {% endif %}
                <main>
                    {% block content %} {% endblock %}
                </main>
                <script type="javascript" src="{{ mix('js/app.js') }}"></script>
                </body>
                </html>
            """.trimIndent()
        }
    }
}
