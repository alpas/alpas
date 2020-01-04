package dev.alpas

import dev.alpas.console.AlpasCommand
import dev.alpas.console.Command
import dev.alpas.console.ConsoleCommandsServiceProvider
import dev.alpas.console.ConsoleKernel
import dev.alpas.exceptions.ExceptionHandler
import dev.alpas.http.HttpKernel
import dev.alpas.notifications.NotificationServiceProvider
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

open class Alpas(args: Array<String>, entryClass: Class<*>, block: Alpas.() -> Unit = {}) : Container by DefaultContainer(),
    AppBase(args, entryClass) {
    constructor(args: Array<String> = emptyArray(), block: Alpas.() -> Unit = {}) : this(
        args,
        StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).callerClass,
        block
    )

    private val consoleCommands = mutableListOf<Command>()
    private val bufferedDebugLogs = mutableListOf<String>()
    private var appState = AppState.CREATED
    private val packageClassLoader: PackageClassLoader by lazy { PackageClassLoader(srcPackage) }
    private val serviceProviders = mutableListOf<ServiceProvider>()

    init {
        registerCoreServices(args)
        loadKernel()
        block()
    }

    override fun bufferDebugLog(log: String) {
        ensureNotInFlight()
        bufferedDebugLogs.add(log)
    }

    override fun ignite() {
        ensureNotInFlight()
        if (shouldLoadConsoleCommands()) {
            igniteCommands()
        }
        kernel.boot(this)
        Runtime.getRuntime().addShutdownHook(Thread { stop() })
        appState = AppState.IGNITED
    }

    override fun takeOff() {
        ensureNotInFlight()
        dispatchBufferedDebugLogs()
        logger.debug { "Running the app" }
        // todo: fix refactor
        // configs.forEach { config -> config.boot(this) }
        serviceProviders.forEach { provider -> provider.boot(this, packageClassLoader) }
        packageClassLoader.close()
        appState = AppState.INFLIGHT
    }

    override fun <T : ServiceProvider> registerProvider(provider: T) {
        ensureNotInFlight()
        singleton(provider)
        provider.register(this, packageClassLoader)
        if (shouldLoadConsoleCommands()) {
            consoleCommands.addAll(provider.commands(this))
        }
        serviceProviders.add(provider)
    }

    override fun <T : ServiceProvider> registerProviders(providers: Iterable<KClass<out T>>) {
        ensureNotInFlight()
        providers.forEach {
            if (kernel.accepts(it)) {
                registerProvider(it.createInstance())
            }
        }
    }

    override fun stop() {
        logger.debug { "Stopping the app" }
        kernel.stop(this)
        serviceProviders.forEach {
            it.stop(this)
        }
    }

    private fun igniteCommands() {
        ensureNotInFlight()
        consoleCommands.sortBy { it.commandName }
        make<AlpasCommand>().addCommands(consoleCommands)
        consoleCommands.clear()
    }

    final override fun loadKernel() {
        registerProviders(kernel.serviceProviders(this))
    }

    private fun ensureNotInFlight() {
        check(appState != AppState.INFLIGHT) { "Cannot perform the operation once the app is already in fight." }
    }

    private fun registerCoreServices(args: Array<String>) {
        // We need to load env variables first as some of the service providers,
        // commands, and configs may depend on an environment variable.
        registerProvider(EnvironmentServiceProvider())
        // Since the logger is not configured yet, we'd buffer any debug logs and once
        // it is initialized, we'll dispatch these buffered logs to the actual logger.
        bufferDebugLog("Registered environment provider")
        // Let's add some core classes such as configs, exception handler etc. that may have
        // been extended in the userland. If these classes are not extended in userland, the
        // consumer of these core classes are required to bind the dependencies themselves.
        loadUserlandCoreClasses(packageClassLoader)
        bufferDebugLog("Registered core userland classes")
        // the configs should be ready by now, let's register them
        configs.forEach { config ->
            config.register(this)
        }
        bufferDebugLog("Registered configs")
        if (shouldLoadConsoleCommands()) {
            registerProvider(ConsoleCommandsServiceProvider(args))
        }
        singleton(ResourceLoader(entryClass))
        registerProvider(NotificationServiceProvider())
        bufferDebugLog("Finished registering core service providers")
    }

    private fun dispatchBufferedDebugLogs() {
        bufferedDebugLogs.forEach(logger::debug)
        bufferedDebugLogs.clear()
    }

    protected open fun shouldLoadConsoleCommands(): Boolean {
        return env.runMode.isConsole()
    }

    private fun loadUserlandCoreClasses(loader: PackageClassLoader) {
        loader.load {
            listOf(Config::class).forEach { interfaze ->
                classesImplementing(interfaze) {
                    bufferDebugLog("Loading $it from $packageName that implement $interfaze")
                    if (it.superclass == null) {
                        // this means this class implements an interface and doesn't extend a class
                        singleton(it.loadClass())
                    } else {
                        singleton(it.superclass, it.loadClass())
                    }
                }
            }

            val extenders: MutableList<KClass<out Any>> = mutableListOf(ExceptionHandler::class)
            if (env.runMode.isConsole()) {
                extenders.add(ConsoleKernel::class)
            } else {
                extenders.add(HttpKernel::class)
            }
            extenders.forEach { extender ->
                classesExtending(extender) {
                    bufferDebugLog("Loading $it from $packageName that implement $extender")
                    bind(extender.java, it.loadClass())
                }
            }
        }
    }

    private enum class AppState {
        CREATED,
        IGNITED,
        INFLIGHT
    }
}

