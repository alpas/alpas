package dev.alpas.tests.views

import dev.alpas.AppConfig
import dev.alpas.Environment
import dev.alpas.http.HttpCall
import dev.alpas.make
import dev.alpas.validation.SharedDataBag
import dev.alpas.view.CustomTags
import dev.alpas.view.ViewConfig
import dev.alpas.view.ViewRenderer
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CustomTagsTest {
    @Test
    fun `can register and render a custom tag`() {
        val app = AlpasTest()
        bindDependencies(app)
        TestViewServiceProvider.apply {
            register(app)
            app.make<CustomTags>().add("greet") { "<h1>hello</h1>" }
            boot(app)
        }
        val sharedDataBag = SharedDataBag().apply { add("call" to mockk<HttpCall>()) }
        val html = app.make<ViewRenderer>().render("<div>{% greet %}</div>", sharedDataBag)
        assertEquals("<div><h1>hello</h1></div>", html)
    }

    @Test
    fun `custom tag will be evaluated`() {
        val app = AlpasTest()
        bindDependencies(app)
        TestViewServiceProvider.apply {
            register(app)
            app.make<CustomTags>().add("greet") { "<h1>hello {{ name }}</h1>" }
            boot(app)
        }
        val sharedDataBag = SharedDataBag().apply {
            add("name" to "john", "call" to mockk<HttpCall>(), "time" to "afternoon")
        }
        val html = app.make<ViewRenderer>().render("<div>{% greet %} good {{ time }}</div>", sharedDataBag)
        assertEquals("<div><h1>hello john</h1> good afternoon</div>", html)
    }

    @Test
    fun `custom tag will have access to standard functions`() {
        val app = AlpasTest()
        bindDependencies(app)
        TestViewServiceProvider.apply {
            register(app)
            app.make<CustomTags>().add("greet") { "<h1>hello {{ name | upper }}</h1>" }
            boot(app)
        }
        val sharedDataBag = SharedDataBag().apply {
            add("name" to "john", "call" to mockk<HttpCall>(), "time" to "afternoon")
        }
        val html = app.make<ViewRenderer>().render("<div>{% greet %} good {{ time }}</div>", sharedDataBag)
        assertEquals("<div><h1>hello JOHN</h1> good afternoon</div>", html)
    }

    private fun bindDependencies(app: AlpasTest): Environment {
        return TestEnv(emptyList()).also { env ->
            app.bind(Environment::class, env)
            app.bind(AppConfig::class, AppConfig(env))
            app.bind(ViewConfig::class, ViewConfig(env))
        }
    }
}
