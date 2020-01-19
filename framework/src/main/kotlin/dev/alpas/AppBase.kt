package dev.alpas

import dev.alpas.console.ConsoleKernel
import dev.alpas.http.HttpKernel
import dev.alpas.routing.Router
import mu.KLogger
import mu.KotlinLogging
import java.io.File
import java.net.URI
import kotlin.reflect.KClass

abstract class AppBase(val args: Array<String>, override var entryClass: Class<*>) : Application {
    val basePath: URI by lazy { entryClass.protectionDomain.codeSource.location.toURI() }
    val rootDir: File by lazy {
        val uri = basePath
        val file = File(uri.path.removeSuffix("/build/classes/kotlin/main/"))
        // if the working directory is actually a file, it means the app ir running from a .jar
        // file in which case we'll use its parent directory as the working directory path
        if (file.isFile) file.parentFile else file
    }
    override val cwd by lazy { File("").absoluteFile }
    override val srcPackage: String by lazy { entryClass.`package`.name }
    override val logger: KLogger by lazy { KotlinLogging.logger("alpas") }
    override val env by lazy { make<Environment>() }
    override val configs by lazy { makeMany<Config>() }

    override val kernel by lazy { if (env.inConsoleMode) makeElse { ConsoleKernel() } else makeElse { HttpKernel() } }

    // The following methods sort of describe the lifecycle of an Alpas application
    // 1. Initialization: First the command line args and the caller classes are registered
    // 2. The kernel is loaded based on the environment
    protected abstract fun loadKernel()

    // 3. The application is "ignited"; not ready to takeoff yet but this allows the app to prepare itself
    abstract override fun ignite()

    // 4. App finally takes off
    abstract override fun takeOff()

    // 5. Gracefully shutdown the app
    abstract override fun stop()

    // Register a service provider of type T
    abstract override fun <T : ServiceProvider> registerProvider(provider: T)

    protected abstract fun <T : ServiceProvider> registerProviders(providers: Iterable<KClass<out T>>)

    // Buffer the debug log while the app is getting ready and while waiting for the real logger to be available
    override fun bufferDebugLog(log: String) {}

    fun routes(block: Router.() -> Unit): Application {
        make<Router>().block()
        return this
    }
}

// Retrieves and returns a config object of type T from the container
inline fun <reified T : Config> Container.config(): T = make()

// A conveinent method that just returns the AppConfig object if it is already added in the container, if not
// this creates a new AppConfig object, registers it to the container and then return the config object.
fun Container.appConfig() = config { AppConfig(it.make()) }

// Returns a config object of type T if already exists in the container otherwise it calls the default callback
// to get an instance of the type T, registers it to the container and then returns the config object.
inline fun <reified T : Config> Container.config(default: (container: Container) -> T): T = makeElse { bind(default(this)) }
