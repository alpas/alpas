package dev.alpas.tests.middleware

import dev.alpas.auth.AuthChannel
import dev.alpas.auth.AuthenticationException
import dev.alpas.auth.middleware.AuthOnlyMiddleware
import dev.alpas.http.HttpCall
import dev.alpas.session.Session
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class AuthOnlyMiddlewareTest {
    @Test
    fun `authenticated call is allowed`(@MockK call: HttpCall, @MockK authChannel: AuthChannel) {
        every { authChannel.check() } returns true
        every { call getProperty "authChannel" } returns authChannel

        var isForwarded = false
        AuthOnlyMiddleware().invoke(call) {
            isForwarded = true
        }

        assertTrue(isForwarded)
    }

    @Test
    fun `unauthenticated call throws an exception`(@RelaxedMockK call: HttpCall, @MockK authChannel: AuthChannel) {
        every { authChannel.check() } returns false
        every { call getProperty "authChannel" } returns authChannel

        var isForwarded = false
        assertThrows(AuthenticationException::class.java) {
            AuthOnlyMiddleware().invoke(call) { isForwarded = true }
        }

        assertFalse(isForwarded)
    }

    @Test
    fun `intended url is not recorded for authorized call`(@RelaxedMockK call: HttpCall, @MockK authChannel: AuthChannel, @RelaxedMockK session: Session) {
        val intendedUrl = "http://localhost/test"
        every { authChannel.check() } returns true
        every { call getProperty "authChannel" } returns authChannel
        every { call getProperty "fullUrl" } returns intendedUrl
        AuthOnlyMiddleware().invoke(call) {}
        verify(exactly = 0) { session.saveIntendedUrl(intendedUrl) }
    }

    @Test
    fun `intended url is recorded for unauthorized call`(@RelaxedMockK call: HttpCall, @MockK authChannel: AuthChannel, @RelaxedMockK session: Session) {
        val intendedUrl = "http://localhost/test"
        every { authChannel.check() } returns false
        every { call getProperty "authChannel" } returns authChannel
        every { call getProperty "session" } returns session
        every { call getProperty "fullUrl" } returns intendedUrl

        assertThrows(AuthenticationException::class.java) {
            AuthOnlyMiddleware().invoke(call) {}
        }
        verify(exactly = 1) { session.saveIntendedUrl(intendedUrl) }
    }
}
