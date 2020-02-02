package dev.alpas.tests.views

import dev.alpas.AppConfig
import dev.alpas.Config
import dev.alpas.Environment
import dev.alpas.view.Mix
import dev.alpas.view.ViewConfig
import dev.alpas.view.extensions.BuiltInExtensions
import dev.alpas.view.extensions.ConditionalTokenParser
import dev.alpas.view.extensions.CsrfTokenParser
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BuiltInExtensionsTest {
    @Test
    fun `functions are added`() {
        val app = AlpasTest().also { bindDependencies(it) }
        val extensions = BuiltInExtensions(app)
        assertEquals(14, extensions.functions.size)
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
            ).joinToString(","), extensions.functions.keys.joinToString(",")
        )
    }

    @Test
    fun `filters are added`() {
        val app = AlpasTest().also { bindDependencies(it) }
        val extensions = BuiltInExtensions(app)
        assertEquals(3, extensions.filters.size)
        assertEquals(
            arrayOf("int", "json_encode", "ago").joinToString(","), extensions.filters.keys.joinToString(",")
        )
    }

    @Test
    fun `token parsers are added`() {
        val app = AlpasTest().also { bindDependencies(it) }
        val extensions = BuiltInExtensions(app)
        assertEquals(3, extensions.tokenParsers.size)
        assertNotNull(extensions.tokenParsers.find { it is CsrfTokenParser })
        assertNotNull(extensions.tokenParsers.find { it is ConditionalTokenParser })
    }

    @Test
    fun `auth token is added`() {
        val app = AlpasTest().also { bindDependencies(it) }
        val extensions = BuiltInExtensions(app)
        val conditionalTag = extensions.tokenParsers.find { it is ConditionalTokenParser && it.tag == "auth" }
        assertNotNull(conditionalTag)
    }

    @Test
    fun `guest token is added`() {
        val app = AlpasTest().also { bindDependencies(it) }
        val extensions = BuiltInExtensions(app)
        val conditionalTag = extensions.tokenParsers.find { it is ConditionalTokenParser && it.tag == "guest" }
        assertNotNull(conditionalTag)
    }

    @Test
    fun `configs and env are available as a global variable`() {
        val app = AlpasTest().also { bindDependencies(it) }
        val extensions = BuiltInExtensions(app)
        val variables = extensions.globalVariables
        assertTrue(variables.containsKey("_configs"))
        assertTrue(variables.containsKey("_env"))
    }

    @Test
    fun `env variables are properly added`() {
        val app = AlpasTest().also { bindDependencies(it) }
        val extensions = BuiltInExtensions(app)
        val envVars = extensions.globalVariables["_env"] as Map<*, *>
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
        val extensions = BuiltInExtensions(app)
        val configVars = extensions.globalVariables["_configs"] as Map<*, *>
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
}

class TestConfig : Config {
    val foo = "bar"
    val hello = "world"
    val test = 123
}

