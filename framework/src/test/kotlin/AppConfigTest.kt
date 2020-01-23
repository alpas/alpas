package dev.alpas.tests

import dev.alpas.AppConfig
import dev.alpas.Environment
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration
import java.time.ZoneOffset

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class AppConfigTest {
    @Test
    fun `default values are correct`(@RelaxedMockK env: Environment) {
        env.answerWithDefaultValues()
        AppConfig(env).apply {
            assertFalse(enableNetworkShare)
            assertEquals(8080, appPort)
            assertEquals("", appUrl)
            assertEquals(Duration.ofMinutes(1), connectionTimeOut)
            assertEquals(2, staticDirs.size)
            assertEquals("/web", staticDirs[0])
            assertEquals("/test/storage/app/public", staticDirs[1])
            assertNull(encryptionKey)
            assertEquals(200, maxThreads)
            assertEquals(8, minThreads)
            assertEquals(ZoneOffset.UTC, timezone)
            assertEquals("app_log_config.xml", appLogConfig)
            assertEquals("console_log_config.xml", consoleLogConfig)
            assertEquals(emptyMap<String, List<String>>(), commandAliases)
            assertTrue(allowMethodSpoofing)
            assertFalse(throwOnMissingStaticDirectories)
        }
    }

    @Test
    fun `sets default values from the environment values`(@RelaxedMockK env: Environment) {
        env.fillWithTestValues()
        AppConfig(env).apply {
            assertTrue(enableNetworkShare)
            assertEquals(appPort, 9090)
            assertEquals(appUrl, "/test")
            assertEquals(encryptionKey, "testappkey")
            assertEquals(maxThreads, 100)
            assertEquals(minThreads, 4)
            assertEquals(appLogConfig, "appconfig")
            assertEquals(consoleLogConfig, "consoleconfig")
        }
    }

    private fun Environment.fillWithTestValues() {
        val env = this
        every { env("ENABLE_NETWORK_SHARE", any<Boolean>()) } returns true
        every { env("APP_PORT", any<Int>()) } returns 9090
        every { env("APP_URL", any<String>()) } returns "/test"
        every { env("APP_KEY") } returns "testappkey"
        every { env("APP_MAX_THREADS", any<Int>()) } returns 100
        every { env("APP_MIN_THREADS", any<Int>()) } returns 4
        every { env("APP_LOG_CONFIG", any<String>()) } returns "appconfig"
        every { env("CONSOLE_LOG_CONFIG", any<String>()) } returns "consoleconfig"
    }

    private fun Environment.answerWithDefaultValues() {
        val env = this

        every { env getProperty "storagePath" } returns "/test/storage"

        val networkShareSlot = slot<Boolean>()
        every { env("ENABLE_NETWORK_SHARE", capture(networkShareSlot)) } answers { networkShareSlot.captured }

        val appPortSlot = slot<Int>()
        every { env("APP_PORT", capture(appPortSlot)) } answers { appPortSlot.captured }

        every { env("APP_KEY") } returns null

        val appUrlSlot = slot<String>()
        every { env("APP_URL", capture(appUrlSlot)) } answers { appUrlSlot.captured }

        val maxThreadSlot = slot<Int>()
        every { env("APP_MAX_THREADS", capture(maxThreadSlot)) } answers { maxThreadSlot.captured }

        val minThreadSlot = slot<Int>()
        every { env("APP_MIN_THREADS", capture(minThreadSlot)) } answers { minThreadSlot.captured }

        val appLogSlot = slot<String>()
        every { env("APP_LOG_CONFIG", capture(appLogSlot)) } answers { appLogSlot.captured }

        val consoleLogSlot = slot<String>()
        every { env("CONSOLE_LOG_CONFIG", capture(consoleLogSlot)) } answers { consoleLogSlot.captured }
    }
}
