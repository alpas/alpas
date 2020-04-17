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

    /**
     * Bind the given [instance] of type [T].
     *
     * If the type of T is a string, it first gets the Class object associated with that class
     * or interface and binds that instead. Otherwise, it binds as the instance itself.
     *
     * @param instance The instance to register in the container.
     */
    fun <T> bind(instance: T): T {
        if (instance is String) {
            container().addComponent(Class.forName(instance))
        } else {
            container().addComponent(instance)
        }
        return instance
    }

    /**
     * Bind the given concrete instance of type [CONCRETE] under the given contract type [CONTRACT].
     *
     * @return The instance itself for further chaining.
     */
    fun <CONTRACT, CONCRETE : CONTRACT> bind(contract: CONTRACT, instance: CONCRETE): CONTRACT {
        container().addComponent(contract, instance)
        return instance
    }

    /**
     * Bind a concrete Class of type [T].
     *
     * e.g. bind(Concrete::class.java)
     *
     * @param concrete A concrete class of type [T] to bind.
     */
    fun <T> bind(concrete: Class<T>) {
        container().addComponent(concrete)
    }

    /**
     * Bind a concrete class of type [CONCRETE] under the contract type [CONTRACT].
     *
     * @param contract The contract to use for binding the concrete Class.
     * @param concreteClass The Class to bind.
     */
    fun <CONTRACT, CONCRETE : CONTRACT> bind(contract: Class<CONTRACT>, concreteClass: Class<CONCRETE>) {
        container().addComponent(contract, concreteClass)
    }

    /**
     * Register the given instance as a singleton.
     *
     * @param singleton The object to be registered as a singleton.
     */
    fun <T> singleton(singleton: T): T {
        singletonContainer().addComponent(singleton)
        return singleton
    }

    /**
     * Bind a concrete class of type [CONCRETE] as a singleton under the contract type [CONTRACT].
     *
     * @param contract The contract to use for binding the concrete Class.
     * @param instance The instance object to bind.
     */
    fun <CONTRACT, CONCRETE : CONTRACT> singleton(contract: CONTRACT, instance: CONCRETE): CONTRACT {
        singletonContainer().addComponent(contract, instance)
        return instance
    }

    /**
     * Remove the given child container from this container.
     *
     * @param container The child container to be removed.
     */
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

/**
 * Register the given factory that returns a type of [T] every time it is invoked.
 *
 * @param factory The factory responsible for returning an instance of type [T] .
 */
inline fun <reified T : Any> Container.bindFactory(crossinline factory: () -> T) {
    picoContainer.`as`(NO_SINGLE).factory(factory)
}

/**
 * Register the given factory as a singleton. This factory should returns a type of [T] every time it is invoked.
 *
 * @param factory The singleton factory responsible for returning an instance of type [T] .
 */
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

/**
 * Try to return an instance of the the given type [T]. If it doesn't exist, return an
 * instance of the same type by invoking the given [defaultFactory].
 *
 * This method doesn't bind the instance resolved by calling the provided [defaultFactory].
 *
 * @param defaultFactory The factory to use for resolving an instance if an instance is not already bound.
 */
inline fun <reified T : Any> Container.makeElse(defaultFactory: Container.(container: Container) -> T): T {
    return tryMake() ?: defaultFactory(this)
}

/**
 * Try to make an instance of the given type [T]. If it doesn't exist, get an instance of the same
 * type by invoking the given [defaultFactory], bind it, and then return the instance.
 *
 * This method binds the instance resolved by calling the provided [defaultFactory].
 *
 * @param defaultFactory The factory to use for resolving an instance if an instance is not already bound.
 */
inline fun <reified T : Any> Container.makeOrBind(defaultFactory: Container.(container: Container) -> T): T {
    return tryMake() ?: defaultFactory(this).also { bind(it) }
}

/**
 * Bind the instance of type [T] returned by the given [defaultFactory] only if such binding already doesn't exist.
 *
 * @param defaultFactory The factory to use for resolving an instance if an instance is not already bound.
 */
inline fun <reified T : Any> Container.bindIfMissing(defaultFactory: Container.(container: Container) -> T) {
    tryMake() ?: defaultFactory(this).also { bind(it) }
}

/**
 * Resolve an instance of type [T] from the container. If it is not registered, return the [default] value.
 *
 * This method doesn't bind the instance.
 */
inline fun <reified T : Any> Container.make(default: T): T {
    return tryMake() ?: default
}

/**
 * Resolve an instance of type [T] from the container.
 * If it is not registered, this method throws a [IllegalStateException].
 *
 */
inline fun <reified T : Any> Container.make(): T {
    return picoContainer.getComponent(T::class.java)
}

/**
 * Resolve an instance of type [T] from the container and invoked the given [block] on it.
 * If it is not registered, this method throws a [IllegalStateException].
 *
 */
inline fun <reified T : Any> Container.make(block: T.() -> Unit): T {
    return picoContainer.getComponent(T::class.java).also(block)
}

/**
 * Resolve many instances of type [T] from the container.
 * If no bindings are registered, this method throws a [IllegalStateException].
 *
 */
inline fun <reified T : Any> Container.makeMany(): List<T> {
    if (picoContainer.parent != null) {
        val parentComponents = picoContainer.parent.getComponents(T::class.java)
        val childComponents = picoContainer.getComponents(T::class.java)
        return childComponents + parentComponents
    }
    return picoContainer.getComponents(T::class.java)
}

/**
 * Try to resolve an instance of the given type [T] without throwing an exception.
 *
 * @return T? An instance of type T if it can be resolved otherwise returns null.
 */
inline fun <reified T : Any> Container.tryMake(): T? {
    return picoContainer.getComponent(T::class.java)
}

/**
 * Create a short-lived instance of the given contract Class type. It first binds itself under the given contract in
 * a new child container and then resolves from it. This allows it to satisfy any of its dependencies as long as
 * the dependencies are registered in this container. This resolved instance cannot be resolved again from this
 * container as it actually does not exist. Use this method to satisfying the dependencies of the given class.
 *
 * @param contract The contract of the class to be resolved.
 * @return T A short-lived instance of type T.
 */
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

/**
 * Create a short-lived instance of the given contract KClass type. It first binds itself under the given contract in
 * a new child container and then resolves from it. This allows it to satisfy any of its dependencies as long as
 * the dependencies are registered in this container. This resolved instance cannot be resolved again from this
 * container as it actually does not exist. Use this method to satisfying the dependencies of the given class.
 *
 * @param contract The contract of the class to be resolved.
 * @return T A short-lived instance of type T.
 */
inline fun <reified T : Any> Container.ephemeral(contract: KClass<*>): T {
    return ephemeral(contract.java)
}

class DefaultContainer(override val picoContainer: MutablePicoContainer = DefaultPicoContainer(OptInCaching())) :
    Container

class ChildContainer(val parent: Container) : Container, AutoCloseable {
    // Even though we have closed a child container, it still has a reference to the parent container and thus will be
    // able to resolve parent's bindings but not its own bindings. I think this is a bug in PicoContainer itself.
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

    inline fun <reified T : Any> makeMany(): List<T> {
        val parentComponents = parent.makeMany<T>()
        val childComponents = picoContainer.getComponents(T::class.java)
        return childComponents + parentComponents
    }
}
