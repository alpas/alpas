package dev.alpas.tests

import dev.alpas.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ContainerTest {
    @Test
    fun `bind and resolve an instance`() {
        val container = DefaultContainer()
        container.bind(ContainerTestObject())
        val obj = container.make<ContainerTestObject>()
        assertNotNull(obj)
        assertEquals(1, initializationCount)
    }

    @Test
    fun `bind an instance creates a new instance of the class only once`() {
        val container = DefaultContainer()
        container.bind(ContainerTestObject())
        val obj1 = container.make<ContainerTestObject>()
        assertNotNull(obj1)
        val obj2 = container.make<ContainerTestObject>()
        assertNotNull(obj2)
        assertEquals(1, initializationCount)
        assertEquals(obj1, obj2)
    }

    @Test
    fun `bind and resolve an instance by class type`() {
        val container = DefaultContainer()
        container.bind(ContainerTestObject::class.java)
        assertNotNull(container.make<ContainerTestObject>())
        assertEquals(1, initializationCount)
    }

    @Test
    fun `bind by class type creates a new instance of the class every time`() {
        val container = DefaultContainer()
        container.bind(ContainerTestObject::class.java)
        val obj1 = container.make<ContainerTestObject>()
        assertNotNull(obj1)
        val obj2 = container.make<ContainerTestObject>()
        assertNotNull(obj2)
        assertEquals(2, initializationCount)
    }

    @Test
    fun `bind and resolve an instance by contract`() {
        val container = DefaultContainer()
        container.bind(ContainerTestObjectContract::class, ContainerTestObject())

        val objByContract = container.make<ContainerTestObjectContract>()
        assertNotNull(objByContract)

        val objByConcrete = container.make<ContainerTestObject>()
        assertNotNull(objByConcrete)
    }

    @Test
    fun `binding an instance by contract creates a new instance of the class only once`() {
        val container = DefaultContainer()
        container.bind(ContainerTestObjectContract::class, ContainerTestObject())

        container.make<ContainerTestObjectContract>()
        container.make<ContainerTestObject>()
        container.make<ContainerTestObject>()

        assertEquals(1, initializationCount)
    }

    @Test
    fun `bind by contract and class type`() {
        val container = DefaultContainer()
        container.bind(ContainerTestObjectContract::class.java, ContainerTestObject::class.java)

        val objByContract = container.make<ContainerTestObjectContract>()
        assertNotNull(objByContract)

        val objByConcrete = container.make<ContainerTestObject>()
        assertNotNull(objByConcrete)
    }

    @Test
    fun `bind by contract and class type creates new instance of the class every time`() {
        val container = DefaultContainer()
        container.bind(ContainerTestObjectContract::class.java, ContainerTestObject::class.java)

        container.make<ContainerTestObjectContract>()
        container.make<ContainerTestObject>()
        container.make<ContainerTestObject>()
        container.make<ContainerTestObjectContract>()

        assertEquals(4, initializationCount)
    }

    @Test
    fun `bind singleton`() {
        val container = DefaultContainer()
        container.singleton(ContainerTestObject::class.java)
        val obj1 = container.make<ContainerTestObject>()
        assertNotNull(obj1)
        val obj2 = container.make<ContainerTestObject>()
        assertNotNull(obj1)
        assertEquals(obj1, obj2)

        assertEquals(1, initializationCount)
    }

    @Test
    fun `bind singleton by contract`() {
        val container = DefaultContainer()
        container.singleton(ContainerTestObjectContract::class.java, ContainerTestObject::class.java)
        val obj1 = container.make<ContainerTestObject>()
        assertNotNull(obj1)
        val obj2 = container.make<ContainerTestObject>()
        assertNotNull(obj2)

        val obj3 = container.make<ContainerTestObjectContract>()
        assertNotNull(obj3)

        assertEquals(obj1, obj2)
        assertEquals(obj2, obj3)

        assertEquals(1, initializationCount)
    }

    @Test
    fun `bind factory`() {
        val container = DefaultContainer()
        var factoryInvokeCount = 0
        container.bindFactory {
            factoryInvokeCount++
            ContainerTestObject()
        }
        val obj1 = container.make<ContainerTestObject>()
        assertNotNull(obj1)
        val obj2 = container.make<ContainerTestObject>()
        val obj3 = container.make<ContainerTestObjectContract>()

        assertNotEquals(obj1, obj2)
        assertNotEquals(obj2, obj3)

        assertEquals(3, initializationCount)
        assertEquals(3, factoryInvokeCount)
    }

    @Test
    fun `bind singleton factory`() {
        val container = DefaultContainer()
        var factoryInvokeCount = 0
        container.singletonFactory {
            factoryInvokeCount++
            ContainerTestObject()
        }
        val obj1 = container.make<ContainerTestObject>()
        assertNotNull(obj1)
        val obj2 = container.make<ContainerTestObject>()
        val obj3 = container.make<ContainerTestObjectContract>()

        assertEquals(obj1, obj2)
        assertEquals(obj2, obj3)

        assertEquals(1, initializationCount)
        assertEquals(1, factoryInvokeCount)
    }

    @Test
    fun `resolve dependencies by contract`() {
        val container = DefaultContainer()
        container.bind(ContainerTestObject::class.java)
        container.bind(ContainerTestDependentObject::class.java)
        val dep = container.make<ContainerTestDependentObject>()
        assertNotNull(dep)
    }

    @Test
    fun `resolve dependencies by concrete class`() {
        val container = DefaultContainer()
        val instance = ContainerTestObject()
        container.bind(instance)
        container.bind(ContainerTestDependentObject::class.java)
        val dep = container.make<ContainerTestDependentObject>()
        assertNotNull(dep)
        assertEquals(instance, dep.testObject)
    }

    @Test
    fun `makeElse returns an existing object if exists`() {
        val container = DefaultContainer()
        val instance = ContainerTestObject()
        container.bind(instance)

        val resolved = container.makeElse {
            ContainerTestObject()
        }

        assertEquals(instance, resolved)
        assertEquals(1, initializationCount)
    }

    @Test
    fun `makeElse returns a factory object if it doesn't exist`() {
        val container = DefaultContainer()
        val instance = ContainerTestObject()
        var factoryInvoked = false

        val resolved = container.makeElse {
            factoryInvoked = true
            ContainerTestObject()
        }

        assertNotEquals(instance, resolved)
        assertEquals(2, initializationCount)
        assertTrue(factoryInvoked)
    }

    @Test
    fun `makeElse doesn't bind the factory object`() {
        val container = DefaultContainer()
        container.makeElse {
            ContainerTestObject()
        }
        assertThrows<IllegalStateException> { container.make<ContainerTestObject>() }
    }

    @Test
    fun `makeOrBind returns an existing object if exists`() {
        val container = DefaultContainer()
        val instance = ContainerTestObject()
        container.bind(instance)

        val resolved = container.makeOrBind {
            ContainerTestObject()
        }

        assertEquals(instance, resolved)
        assertEquals(1, initializationCount)
    }

    @Test
    fun `makeOrBind returns a factory object if it doesn't exist`() {
        val container = DefaultContainer()
        val instance = ContainerTestObject()
        var factoryInvoked = false

        val resolved = container.makeOrBind {
            factoryInvoked = true
            ContainerTestObject()
        }

        assertNotEquals(instance, resolved)
        assertEquals(2, initializationCount)
        assertTrue(factoryInvoked)
    }

    @Test
    fun `makeOrBind binds the factory object`() {
        val container = DefaultContainer()
        container.makeOrBind {
            ContainerTestObject()
        }
        val instance = container.make<ContainerTestObject>()
        assertNotNull(instance)
    }

    @Test
    fun `bindIfMissing binds the factory object`() {
        val container = DefaultContainer()
        container.bindIfMissing {
            ContainerTestObject()
        }
        val instance = container.make<ContainerTestObject>()
        assertNotNull(instance)
    }

    @Test
    fun `make with receiver lambda`() {
        val container = DefaultContainer()
        container.bind(ContainerTestObject())
        var callbackObject: ContainerTestObject? = null
        container.make<ContainerTestObject>() {
            callbackObject = this
        }
        assertNotNull(callbackObject)
    }

    @Test
    fun `resolve ephemeral class`() {
        val container = DefaultContainer()
        val instance = ContainerTestObject()
        container.bind(instance)
        val dep = container.ephemeral(ContainerTestDependentObject::class.java) as? ContainerTestDependentObject
        assertNotNull(dep)
        assertEquals(instance, dep?.testObject)
    }

    @Test
    fun `resolve ephemeral kotlin class`() {
        val container = DefaultContainer()
        val instance = ContainerTestObject()
        container.bind(instance)
        val dep = container.ephemeral(ContainerTestDependentObject::class.java.kotlin) as? ContainerTestDependentObject
        assertNotNull(dep)
        assertEquals(instance, dep?.testObject)
    }

    @Test
    fun `ephemeral class is short lived and not bound`() {
        val container = DefaultContainer()
        val instance = ContainerTestObject()
        container.bind(instance)
        container.ephemeral(ContainerTestDependentObject::class.java) as? ContainerTestDependentObject
        assertThrows<IllegalStateException> { container.make<ContainerTestDependentObject>() }
    }

    @Test
    fun `resolve multiple instances`() {
        val container = DefaultContainer()
        container.bind(ContainerTestObject())
        container.bind(ContainerTestObject2())
        val instances = container.makeMany<ContainerTestObjectContract>()
        assertEquals(2, instances.count())
    }

    @Test
    fun `resolve multiple instances include components from parents`() {
        val container = DefaultContainer()
        container.bind(ContainerTestObject())
        container.bind(ContainerTestObject2())
        val childContainer = ChildContainer(container)
        childContainer.bind(ContainerTestObject2())
        val instancesThroughChild = childContainer.makeMany<ContainerTestObjectContract>()
        assertEquals(3, instancesThroughChild.count())
    }

    @Test
    fun `unresolved binding will throw an exception`() {
        assertThrows<IllegalStateException> { DefaultContainer().make<ContainerTestObject>() }
    }

    @Test
    fun `child container can resolve parent's bindings`() {
        val container = DefaultContainer()
        container.bind(ContainerTestObject())
        val childContainer = ChildContainer(container)
        childContainer.bind(ContainerTestObject2())
        assertNotNull(childContainer.make<ContainerTestObject>())
        assertNotNull(childContainer.make<ContainerTestObject2>())
    }

    @Test
    fun `parent container cannot resolve children's bindings`() {
        val container = DefaultContainer()
        container.bind(ContainerTestObject())
        val childContainer = ChildContainer(container)
        childContainer.bind(ContainerTestObject2())
        assertThrows<IllegalStateException> { container.make<ContainerTestObject2>() }
    }

    @Test
    fun `once closed child container cannot resolve own bindings`() {
        val container = DefaultContainer()
        val childContainer = ChildContainer(container)
        childContainer.bind(ContainerTestObject2())
        childContainer.close()
        assertThrows<IllegalStateException> { childContainer.make<ContainerTestObject2>() }
    }

    @BeforeEach
    fun beforeEach() {
        initializationCount = 0
    }
}

private var initializationCount = 0

internal interface ContainerTestObjectContract

internal class ContainerTestObject : ContainerTestObjectContract {
    init {
        initializationCount++
    }
}

internal class ContainerTestObject2 : ContainerTestObjectContract {
    init {
        initializationCount++
    }
}

internal class ContainerTestDependentObject(val testObject: ContainerTestObjectContract)
