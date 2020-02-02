package dev.alpas.tests.views

import com.mitchellbosecke.pebble.loader.Loader
import com.mitchellbosecke.pebble.loader.StringLoader
import dev.alpas.*
import dev.alpas.view.PebbleViewRenderer
import dev.alpas.view.ViewConfig
import dev.alpas.view.ViewRenderer
import dev.alpas.view.ViewServiceProvider
import kotlin.reflect.KClass

object TestViewServiceProvider : ViewServiceProvider() {
    override fun viewRenderer(app: Application): ViewRenderer {
        return TestPebbleTemplateRenderer(app)
    }
}

class TestPebbleTemplateRenderer(app: Application) : PebbleViewRenderer(app) {
    override fun loader(app: Application, config: ViewConfig): Loader<String> {
        return StringLoader()
    }
}

class AlpasTest : Container by DefaultContainer(), AppBase(emptyArray(), AlpasTest::class.java) {
    override fun loadKernel() {}

    override fun ignite() {}

    override fun takeOff() {}

    override fun stop() = picoContainer.dispose()

    override fun <T : ServiceProvider> registerProvider(provider: T) {}

    override fun <T : ServiceProvider> registerProviders(providers: Iterable<KClass<out T>>) {}
}
