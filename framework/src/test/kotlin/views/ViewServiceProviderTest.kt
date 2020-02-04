package dev.alpas.tests.views

import com.mitchellbosecke.pebble.extension.AbstractExtension
import dev.alpas.*
import dev.alpas.view.*
import dev.alpas.view.extensions.PebbleExtensionWrapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ViewServiceProviderTest {
    @Test
    fun `view config is not registered if view config is disabled`() {
        val app = AlpasTest()
        val env = bindEnv(app)

        var isRegistered = false
        val viewConfig = object : ViewConfig(env) {
            override val isEnabled = false
            override fun register(app: Application) {
                isRegistered = true
            }
        }
        app.bind(ViewConfig::class, viewConfig)
        ViewServiceProvider().register(app, PackageClassLoader(app.srcPackage))

        assertFalse(isRegistered)
    }

    @Test
    fun `view config is registered if view config is enabled`() {
        val app = AlpasTest()
        val env = bindEnv(app)

        var isRegistered = false
        val viewConfig = object : ViewConfig(env) {
            override val isEnabled = true
            override fun register(app: Application) {
                isRegistered = true
            }
        }
        app.bind(ViewConfig::class, viewConfig)
        ViewServiceProvider().register(app, PackageClassLoader(app.srcPackage))

        assertTrue(isRegistered)
    }

    @Test
    fun `view related classes are not bound if view config is disabled`() {
        val app = AlpasTest()
        val env = bindEnv(app)
        val viewConfig = viewConfig(env, false)

        app.bind(ViewConfig::class, viewConfig)
        ViewServiceProvider().register(app, PackageClassLoader(app.srcPackage))

        assertNull(app.tryMake<ConditionalTags>())
        assertNull(app.tryMake<CustomTags>())
        assertNull(app.tryMake<Mix>())
        assertNull(app.tryMake<ViewRenderer>())
    }

    @Test
    fun `view related classes are bound if view config is enabled`() {
        val app = AlpasTest()
        val env = bindEnv(app)
        val viewConfig = viewConfig(env, true)

        app.bind(ViewConfig::class, viewConfig)
        ViewServiceProvider().register(app, PackageClassLoader(app.srcPackage))

        assertNotNull(app.tryMake<ConditionalTags>())
        assertNotNull(app.tryMake<CustomTags>())
        assertNotNull(app.tryMake<Mix>())
        assertNotNull(app.tryMake<ViewRenderer>())
    }

    @Test
    fun `template extensions is added when booted and is enabled`() {
        val app = AlpasTest()
        val env = bindEnv(app)
        val viewConfig = viewConfig(env, true)
        app.bind(ViewConfig::class, viewConfig)
        val extended = bootServiceProvider(app)

        assertEquals(4, extended.size)
        assertNotNull(extended.find { it is PebbleExtensionWrapper })
    }

    @Test
    fun `template extensions is not added when booted and is disabled`() {
        val app = AlpasTest()
        val env = bindEnv(app)
        val viewConfig = viewConfig(env, false)
        app.bind(ViewConfig::class, viewConfig)
        val extended = bootServiceProvider(app)

        assertEquals(0, extended.size)
    }

    private fun bootServiceProvider(app: AlpasTest): List<AbstractExtension> {
        val extended = mutableListOf<AbstractExtension>()
        val renderer = object : ViewRenderer {
            override fun extend(extension: AbstractExtension, vararg extensions: AbstractExtension) {
                extended.add(extension)
                extended.addAll(extensions)
            }
        }
        val provider = object : ViewServiceProvider() {
            override fun viewRenderer(app: Application) = renderer
        }

        provider.register(app, PackageClassLoader(app.srcPackage))
        provider.boot(app)
        return extended
    }

    private fun viewConfig(env: Environment, isEnabled: Boolean): ViewConfig {
        return object : ViewConfig(env) {
            override val isEnabled = isEnabled
        }
    }

    private fun bindEnv(app: AlpasTest): Environment {
        return TestEnv(emptyList()).also {
            app.bind(Environment::class, it)
            app.bind(AppConfig::class, AppConfig(it))
        }
    }
}
