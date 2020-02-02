package dev.alpas.tests.views

import dev.alpas.AppConfig
import dev.alpas.Environment
import dev.alpas.http.HttpCall
import dev.alpas.make
import dev.alpas.validation.SharedDataBag
import dev.alpas.view.ConditionalTags
import dev.alpas.view.CustomTags
import dev.alpas.view.ViewConfig
import dev.alpas.view.ViewRenderer
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConditionalTagsTest {
    @Test
    fun `conditional tag's if body can be evaluated`() {
        val app = AlpasTest()
        bindDependencies(app)
        TestViewServiceProvider.apply {
            register(app)
            app.make<ConditionalTags>().add("prod") { true }
            boot(app)
        }
        val sharedDataBag = SharedDataBag().apply { add("call" to mockk<HttpCall>()) }
        val html = app.make<ViewRenderer>().render(
            """
                {% prod %}
                <div>yes</div>
                {% else %}
                <p>no</p>
                {% endprod %}
            """.trimIndent(), sharedDataBag
        )
        assertEquals("<div>yes</div>\n", html)
    }

    @Test
    fun `conditional tag's else body can be evaluated`() {
        val app = AlpasTest()
        bindDependencies(app)
        TestViewServiceProvider.apply {
            register(app)
            app.make<ConditionalTags>().add("prod") { false }
            boot(app)
        }
        val sharedDataBag = SharedDataBag().apply { add("call" to mockk<HttpCall>()) }
        val html = app.make<ViewRenderer>().render(
            """
                {% prod %}
                <div>yes</div>
                {% else %}
                <p>no</p>
                {% endprod %}
            """.trimIndent(), sharedDataBag
        )
        assertEquals("<p>no</p>\n", html)
    }

    @Test
    fun `else body is optional`() {
        val app = AlpasTest()
        bindDependencies(app)
        TestViewServiceProvider.apply {
            register(app)
            app.make<ConditionalTags>().add("prod") { true }
            boot(app)
        }
        val sharedDataBag = SharedDataBag().apply { add("call" to mockk<HttpCall>()) }
        val html = app.make<ViewRenderer>().render(
            """
                {% prod %}
                <div>yes</div>
                {% endprod %}
            """.trimIndent(), sharedDataBag
        )
        assertEquals("<div>yes</div>\n", html)
    }

    @Test
    fun `body will have access to context`() {
        val app = AlpasTest()
        bindDependencies(app)
        TestViewServiceProvider.apply {
            register(app)
            app.make<ConditionalTags>().add("prod") { true }
            boot(app)
        }
        val sharedDataBag = SharedDataBag().apply { add("call" to mockk<HttpCall>(), "name" to "John") }
        val html = app.make<ViewRenderer>().render(
            """
                {% prod %}
                <div>hello {{ name }}!</div>
                {% endprod %}
            """.trimIndent(), sharedDataBag
        )
        assertEquals("<div>hello John!</div>\n", html)
    }

    private fun bindDependencies(app: AlpasTest): Environment {
        return TestEnv(emptyList()).also { env ->
            app.bind(Environment::class, env)
            app.bind(AppConfig::class, AppConfig(env))
            app.bind(ViewConfig::class, ViewConfig(env))
        }
    }
}
