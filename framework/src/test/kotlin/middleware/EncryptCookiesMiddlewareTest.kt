package dev.alpas.tests.middleware

import dev.alpas.Environment
import dev.alpas.cookie.CookieJar
import dev.alpas.cookie.EncryptCookies
import dev.alpas.encryption.Encrypter
import dev.alpas.http.HttpCall
import dev.alpas.session.SessionCacheDriver
import dev.alpas.session.SessionConfig
import dev.alpas.session.SessionStoreDriver
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class EncryptCookiesMiddlewareTest {
    @Test
    fun `outgoing cookies are encrypted`(@RelaxedMockK call: HttpCall, @RelaxedMockK sessionConfig: SessionConfig) {
        val encrypter = Encrypter("abcdef")
        every { call.picoContainer.getComponent(Encrypter::class.java) } returns encrypter
        every { call.picoContainer.getComponent(SessionConfig::class.java) } returns sessionConfig
        val cookieJar = CookieJar()
        every { call getProperty "cookie" } returns cookieJar

        call.cookie["cookie1"] = "val1"
        call.cookie["cookie2"] = "val2"
        EncryptCookies().invoke(call) {}


        val c1 = cookieJar.outgoingCookies[0]
        val c2 = cookieJar.outgoingCookies[1]

        assertNotEquals("val1", c1.value)
        assertNotEquals("val2", c2.value)

        assertEquals("val1", encrypter.decrypt(c1.value))
        assertEquals("val2", encrypter.decrypt(c2.value))
    }

    @Test
    fun `call is always forwarded`(@RelaxedMockK call: HttpCall, @RelaxedMockK sessionConfig: SessionConfig) {
        every { call.picoContainer.getComponent(Encrypter::class.java) } returns Encrypter("abcdef")
        every { call.picoContainer.getComponent(SessionConfig::class.java) } returns sessionConfig
        every { call getProperty "cookie" } returns CookieJar()
        var isForwarded = false
        EncryptCookies().invoke(call) {
            isForwarded = true
        }
        assertTrue(isForwarded)
    }

    @Test
    fun `default cookies are not encrypted`(@RelaxedMockK call: HttpCall, @RelaxedMockK env: Environment) {
        val encrypter = Encrypter("abcdef")
        val sessionConfig = TestSessionConfig(env)

        every { call.picoContainer.getComponent(Encrypter::class.java) } returns encrypter
        every { call.picoContainer.getComponent(SessionConfig::class.java) } returns sessionConfig

        val cookieJar = CookieJar()
        every { call getProperty "cookie" } returns cookieJar

        call.cookie[sessionConfig.cookieName] = "test cookie"
        call.cookie["X-CSRF-TOKEN"] = "test csrf token"
        call.cookie["JSESSIONID"] = "jsession cookie"
        call.cookie["should-be-encrypted"] = "must encrypt"

        EncryptCookies().invoke(call) {}

        assertEquals("test cookie", cookieJar.outgoingCookies[0].value)
        assertEquals("test csrf token", cookieJar.outgoingCookies[1].value)
        assertEquals("jsession cookie", cookieJar.outgoingCookies[2].value)

        assertNotEquals("must encrypt", cookieJar.outgoingCookies[3].value)
        assertEquals("must encrypt", encrypter.decrypt(cookieJar.outgoingCookies[3].value))
    }

    @Test
    fun `cookies from an exception list are not encrypted`(@RelaxedMockK call: HttpCall, @RelaxedMockK env: Environment) {
        val encrypter = Encrypter("abcdef")
        val sessionConfig = TestSessionConfig(env)

        every { call.picoContainer.getComponent(Encrypter::class.java) } returns encrypter
        every { call.picoContainer.getComponent(SessionConfig::class.java) } returns sessionConfig

        val cookieJar = CookieJar()
        every { call getProperty "cookie" } returns cookieJar

        call.cookie["except1"] = "except 1"
        call.cookie["except2"] = "except 2"
        call.cookie["except3"] = "except 3"

        EncryptCookies().invoke(call) {}

        assertEquals("except 1", cookieJar.outgoingCookies[0].value)
        assertEquals("except 2", cookieJar.outgoingCookies[1].value)
        assertNotEquals("except 3", cookieJar.outgoingCookies[2].value)
        assertEquals("except 3", encrypter.decrypt(cookieJar.outgoingCookies[2].value))
    }
}

private class TestSessionConfig(env: Environment) : SessionConfig(env) {
    override val cacheDriver = SessionCacheDriver.MEMORY
    override val storeDriver = SessionStoreDriver.SKIP

    override val encryptExcept = listOf("except1", "except2")
}
