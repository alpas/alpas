package dev.alpas.tests.views

import dev.alpas.AppConfig
import dev.alpas.Application
import dev.alpas.Config
import dev.alpas.Environment
import dev.alpas.view.Mix
import dev.alpas.view.ViewConfig
import dev.alpas.view.extensions.PebbleExtensions
import dev.alpas.view.extensions.ConditionalTokenParser
import dev.alpas.view.extensions.CsrfTokenParser
import dev.alpas.view.extensions.PebbleExtensionWrapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PebbleExtensionsTest {
    @Test
    fun `functions are added`() {
        val extensions = builtInExtensions()
        assertEquals(14, extensions.functions?.size)
        assertEquals(
            arrayOf(
                "mix",
                "route",
                "hasRoute",
                "routeIs",
                "routeIsOneOf",
                "old",
                "config",
                "env",
                "errors",
                "hasError",
                "whenError",
                "firstError",
                "flash",
                "hasFlash"
            ).joinToString(","), extensions.functions?.keys?.joinToString(",")
        )
    }

    @Test
    fun `filters are added`() {
        val extensions = builtInExtensions()
        assertEquals(3, extensions.filters?.size)
        assertEquals(
            arrayOf("int", "json_encode", "ago").joinToString(","), extensions.filters?.keys?.joinToString(",")
        )
    }

    @Test
    fun `token parsers are added`() {
        val extensions = builtInExtensions()
        assertEquals(3, extensions.tokenParsers?.size)
        assertNotNull(extensions.tokenParsers?.find { it is CsrfTokenParser })
        assertNotNull(extensions.tokenParsers?.find { it is ConditionalTokenParser })
    }

    @Test
    fun `auth token is added`() {
        val extensions = builtInExtensions()
        val conditionalTag = extensions.tokenParsers?.find { it is ConditionalTokenParser && it.tag == "auth" }
        assertNotNull(conditionalTag)
    }

    @Test
    fun `guest token is added`() {
        val extensions = builtInExtensions()
        val conditionalTag = extensions.tokenParsers?.find { it is ConditionalTokenParser && it.tag == "guest" }
        assertNotNull(conditionalTag)
    }

    @Test
    fun `configs and env are available as a global variable`() {
        val extensions = builtInExtensions()
        val variables = extensions.globalVariables
        assertTrue(variables?.containsKey("_configs") ?: false)
        assertTrue(variables?.containsKey("_env") ?: false)
    }

    @Test
    fun `env variables are properly added`() {
        val extensions = builtInExtensions()
        val envVars = extensions.globalVariables?.get("_env") as Map<*, *>
        assertEquals("test", envVars["APP_NAME"])
        assertEquals("localhost", envVars["URL"])
        assertEquals("SECRET", envVars["KEY"])
    }

    @Test
    fun `config variables are properly added`() {
        val app = AlpasTest().also {
            bindDependencies(it)
            it.bind(TestConfig())
        }
        val extensions = builtInExtensions(app)
        val configVars = extensions.globalVariables?.get("_configs") as Map<*, *>
        assertEquals("bar", configVars["test.foo"])
        assertEquals("world", configVars["test.hello"])
        assertEquals(123, configVars["test.test"])
    }

    private fun bindDependencies(app: AlpasTest): Environment {
        return TestEnv(listOf("APP_NAME" to "test", "URL" to "localhost", "KEY" to "SECRET")).also { env ->
            app.bind(Environment::class, env)
            app.bind(AppConfig::class, AppConfig(env))
            app.bind(ViewConfig::class, ViewConfig(env))
            app.singleton(Mix(app))
        }
    }

    private fun builtInExtensions(app: Application? = null): PebbleExtensionWrapper {
        return PebbleExtensionWrapper(PebbleExtensions(), app ?: AlpasTest().also { bindDependencies(it) })
    }
}

class TestConfig : Config {
    val foo = "bar"
    val hello = "world"
    val test = 123
}

