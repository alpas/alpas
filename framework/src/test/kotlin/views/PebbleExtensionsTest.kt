package dev.alpas.tests.views

import com.mitchellbosecke.pebble.template.EvaluationContext
import dev.alpas.AppConfig
import dev.alpas.Application
import dev.alpas.Config
import dev.alpas.Environment
import dev.alpas.http.HttpCall
import dev.alpas.session.CSRF_SESSION_KEY
import dev.alpas.view.*
import dev.alpas.view.extensions.PebbleExtensionWrapper
import dev.alpas.view.extensions.PebbleExtensions
import io.mockk.every
import io.mockk.mockk
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
    fun `token parsers are added during registration`() {
        val customTagNames = mutableListOf<String>()
        val customTags = object : CustomTags {
            override fun add(tagName: String, callback: TagContext.() -> String) {
                customTagNames.add(tagName)
            }
        }
        val conditionalTagNames = mutableListOf<String>()
        val conditionalTags = object : ConditionalTags {
            override fun add(tagName: String, condition: (context: TagContext) -> Boolean) {
                conditionalTagNames.add(tagName)
            }
        }

        PebbleExtensions().register(AlpasTest(), conditionalTags)
        PebbleExtensions().register(AlpasTest(), customTags)

        assertEquals(1, customTagNames.size)
        assertTrue(customTagNames.contains("csrf"))
        assertEquals(2, conditionalTagNames.size)
        assertTrue(conditionalTagNames.contains("auth"))
        assertTrue(conditionalTagNames.contains("guest"))
    }

    @Test
    fun `csrf token parser render correct field`() {
        val customCallbacks = mutableListOf<TagContext.() -> String>()
        val customTags = object : CustomTags {
            override fun add(tagName: String, callback: TagContext.() -> String) {
                if (tagName == "csrf") customCallbacks.add(callback)
            }
        }
        val conditionalTags = object : ConditionalTags {
            override fun add(tagName: String, condition: (context: TagContext) -> Boolean) {}
        }

        PebbleExtensions().register(AlpasTest(), conditionalTags)
        PebbleExtensions().register(AlpasTest(), customTags)

        val callback = customCallbacks.first()
        val context = mockk<EvaluationContext>()
        every { context.getVariable(CSRF_SESSION_KEY) } returns "test_code"
        val tag = callback(TagContext(mockk(), context, 1, ""))
        assertEquals("""<input type="hidden" name="_csrf" value="test_code">""", tag)
    }

    @Test
    fun `auth token parser evaluates if a call is authenticated`() {
        val conditionalCallbacks = mutableListOf<TagContext.() -> Boolean>()
        val customTags = object : CustomTags {
            override fun add(tagName: String, callback: TagContext.() -> String) {}
        }
        val conditionalTags = object : ConditionalTags {
            override fun add(tagName: String, condition: TagContext.() -> Boolean) {
                if (tagName == "auth") conditionalCallbacks.add(condition)
            }
        }

        PebbleExtensions().register(AlpasTest(), conditionalTags)
        PebbleExtensions().register(AlpasTest(), customTags)

        val callback = conditionalCallbacks.first()
        val call = mockk<HttpCall>()
        every { call.isAuthenticated } returns true

        val condition = callback(TagContext(call, mockk(), 1, ""))
        assertTrue(condition)
    }

    @Test
    fun `auth token parser does not if a call is not authenticated`() {
        val conditionalCallbacks = mutableListOf<TagContext.() -> Boolean>()
        val customTags = object : CustomTags {
            override fun add(tagName: String, callback: TagContext.() -> String) {}
        }
        val conditionalTags = object : ConditionalTags {
            override fun add(tagName: String, condition: TagContext.() -> Boolean) {
                if (tagName == "auth") conditionalCallbacks.add(condition)
            }
        }

        PebbleExtensions().register(AlpasTest(), conditionalTags)
        PebbleExtensions().register(AlpasTest(), customTags)

        val callback = conditionalCallbacks.first()
        val call = mockk<HttpCall>()
        every { call.isAuthenticated } returns false

        val condition = callback(TagContext(call, mockk(), 1, ""))
        assertFalse(condition)
    }

    @Test
    fun `guest token parser evaluates if a call is authenticated`() {
        val conditionalCallbacks = mutableListOf<TagContext.() -> Boolean>()
        val customTags = object : CustomTags {
            override fun add(tagName: String, callback: TagContext.() -> String) {}
        }
        val conditionalTags = object : ConditionalTags {
            override fun add(tagName: String, condition: TagContext.() -> Boolean) {
                if (tagName == "guest") conditionalCallbacks.add(condition)
            }
        }

        PebbleExtensions().register(AlpasTest(), conditionalTags)
        PebbleExtensions().register(AlpasTest(), customTags)

        val callback = conditionalCallbacks.first()
        val call = mockk<HttpCall>()
        every { call.isAuthenticated } returns true

        val condition = callback(TagContext(call, mockk(), 1, ""))
        assertFalse(condition)
    }

    @Test
    fun `guest token parser is not evaluated if a call is not authenticated`() {
        val conditionalCallbacks = mutableListOf<TagContext.() -> Boolean>()
        val customTags = object : CustomTags {
            override fun add(tagName: String, callback: TagContext.() -> String) {}
        }
        val conditionalTags = object : ConditionalTags {
            override fun add(tagName: String, condition: TagContext.() -> Boolean) {
                if (tagName == "guest") conditionalCallbacks.add(condition)
            }
        }

        PebbleExtensions().register(AlpasTest(), conditionalTags)
        PebbleExtensions().register(AlpasTest(), customTags)

        val callback = conditionalCallbacks.first()
        val call = mockk<HttpCall>()
        every { call.isAuthenticated } returns false

        val condition = callback(TagContext(call, mockk(), 1, ""))
        assertTrue(condition)
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

