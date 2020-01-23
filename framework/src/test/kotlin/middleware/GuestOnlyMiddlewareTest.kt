package dev.alpas.tests.middleware

import dev.alpas.auth.AuthChannel
import dev.alpas.auth.AuthConfig
import dev.alpas.auth.middleware.GuestOnlyMiddleware
import dev.alpas.http.HttpCall
import dev.alpas.http.Redirector
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class GuestOnlyMiddlewareTest {
    @Test
    fun `guest call is allowed`(@MockK call: HttpCall, @MockK authChannel: AuthChannel) {
        every { authChannel.isLoggedIn() } returns false
        every { call getProperty "authChannel" } returns authChannel

        var isForwarded = false
        GuestOnlyMiddleware().invoke(call) { isForwarded = true }

        assertTrue(isForwarded)
    }

    @Test
    fun `authenticated call is redirected`(@RelaxedMockK call: HttpCall, @MockK authChannel: AuthChannel, @MockK authConfig: AuthConfig, @RelaxedMockK redirector: Redirector) {
        every { authConfig.ifAuthorizedRedirectToPath(any()) } returns "/login"
        every { authChannel.isLoggedIn() } returns true
        every { call getProperty "authChannel" } returns authChannel
        every { call.redirect() } returns redirector
        every { call.picoContainer.getComponent(AuthConfig::class.java) } returns authConfig

        var isForwarded = false
        GuestOnlyMiddleware().invoke(call) { isForwarded = true }
        assertFalse(isForwarded)

        verify(exactly = 1) { redirector.to("/login") }
    }

    @Test
    fun `guest call is not redirected`(@RelaxedMockK call: HttpCall, @MockK authChannel: AuthChannel, @MockK authConfig: AuthConfig, @RelaxedMockK redirector: Redirector) {
        every { authConfig.ifAuthorizedRedirectToPath(any()) } returns "/login"
        every { authChannel.isLoggedIn() } returns false
        every { call getProperty "authChannel" } returns authChannel
        every { call.redirect() } returns redirector
        every { call.picoContainer.getComponent(AuthConfig::class.java) } returns authConfig

        GuestOnlyMiddleware().invoke(call) {}

        verify(exactly = 0) { redirector.to("/login") }
    }
}
