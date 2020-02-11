package dev.alpas

import org.picocontainer.Characteristics.NO_SINGLE
import org.picocontainer.Characteristics.SINGLE
import org.picocontainer.DefaultPicoContainer
import org.picocontainer.MutablePicoContainer
import org.picocontainer.PicoContainer
import org.picocontainer.behaviors.OptInCaching
import org.picocontainer.injectors.FactoryInjector
import java.lang.reflect.Type
import kotlin.reflect.KClass

@Suppress("unused")
interface Container {
    val picoContainer: MutablePicoContainer

    fun <T> bind(instance: T): T {
        if (instance is String) {
            container().addComponent(Class.forName(instance))
        } else {
            container().addComponent(instance)
        }
        return instance
    }

    fun <T, S : T> bind(contract: T, instance: S): T {
        container().addComponent(contract, instance)
        return instance
    }

    fun <T> bind(concrete: Class<T>) {
        container().addComponent(concrete)
    }

    fun <T, S : T> bind(contract: Class<T>, concrete: Class<S>) {
        container().addComponent(contract, concrete)
    }

    fun <T> singleton(singleton: T): T {
        singletonContainer().addComponent(singleton)
        return singleton
    }

    fun <T, S : T> singleton(contract: T, instance: S): T {
        singletonContainer().addComponent(contract, instance)
        return instance
    }

    fun removeChildContainer(container: Container) {
        picoContainer.removeChildContainer(container.picoContainer)
    }

    private fun singletonContainer(): MutablePicoContainer {
        return picoContainer.`as`(SINGLE)
    }

    private fun container(): MutablePicoContainer {
        return picoContainer
    }
}

inline fun <reified T : Any> Container.bindFactory(crossinline factory: () -> T) {
    picoContainer.`as`(NO_SINGLE).factory(factory)
}

inline fun <reified T : Any> Container.singletonFactory(crossinline factory: () -> T) {
    picoContainer.`as`(SINGLE).factory(factory)
}

inline fun <reified T : Any> MutablePicoContainer.factory(crossinline factory: () -> T) {
    this.addAdapter(object : FactoryInjector<T>() {
        override fun getComponentInstance(container: PicoContainer?, into: Type?): T {
            return factory()
        }
    })
}

inline fun <reified T : Any> Container.makeElse(default: Container.(container: Container) -> T): T {
    return tryMake() ?: default(this)
}

inline fun <reified T : Any> Container.makeOrBind(default: Container.(container: Container) -> T): T {
    return tryMake() ?: default(this).also {
        bind(it)
    }
}

inline fun <reified T : Any> Container.bindIfMissing(default: Container.(container: Container) -> T) {
    tryMake() ?: default(this).also { bind(it) }
}

inline fun <reified T : Any> Container.make(default: T): T {
    return tryMake() ?: default
}

inline fun <reified T : Any> Container.make(): T {
    return picoContainer.getComponent(T::class.java)
}

inline fun <reified T : Any> Container.make(block: T.() -> Unit): T {
    return picoContainer.getComponent(T::class.java).also(block)
}

inline fun <reified T : Any> Container.makeMany(): List<T> {
    return picoContainer.getComponents(T::class.java)
}

inline fun <reified T : Any> Container.tryMake(): T? {
    return picoContainer.getComponent(T::class.java)
}

inline fun <reified T : Any> Container.ephemeral(contract: Class<*>): T {
    // To create a short-lived instance, We'll create a temporary child container and
    // resolve the instance before detaching this temporary child container again.
    return ChildContainer(this).let {
        it.bind(contract)
        val instance = it.make<T>()
        it.detachFromParent()
        instance
    }
}

inline fun <reified T : Any> Container.ephemeral(contract: KClass<*>): T {
    return ephemeral(contract.java)
}

class DefaultContainer(override val picoContainer: MutablePicoContainer = DefaultPicoContainer(OptInCaching())) :
    Container

class ChildContainer(val parent: Container) : Container, AutoCloseable {
    override fun close() {
        picoContainer.components.forEach {
            picoContainer.removeComponentByInstance(it)
        }
        detachFromParent()
    }

    override val picoContainer: MutablePicoContainer = parent.picoContainer.makeChildContainer()

    fun detachFromParent() {
        parent.removeChildContainer(this)
        picoContainer.dispose()
    }
}
