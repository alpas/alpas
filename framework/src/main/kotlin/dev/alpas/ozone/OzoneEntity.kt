package dev.alpas.ozone

import me.liuwj.ktorm.entity.Entity
import me.liuwj.ktorm.schema.TypeReference
import java.lang.reflect.Proxy
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.jvmErasure

interface OzoneEntity<E : OzoneEntity<E>> : Entity<E> {
    abstract class Of<E : OzoneEntity<E>> : TypeReference<E>() {
        /**
         * Overload the `invoke` operator, creating an Ozone object just like there is a constructor.
         */
        @Suppress("UNCHECKED_CAST")
        operator fun invoke(): E {
            return Entity.create(referencedKotlinType.jvmErasure) as E
        }

        /**
         * Overload the `invoke` operator, creating an Ozone object and call the [init] function.
         */
        inline operator fun invoke(init: E.() -> Unit): E {
            return invoke().apply(init)
        }
    }

    @Suppress("UNCHECKED_CAST")
    val changedProperties: Set<String>
        get() {
            val impl = Proxy.getInvocationHandler(this)
            val changedProperties = impl::class.declaredMemberProperties.first { it.name == "changedProperties" } as KProperty1<Any, Any>
            return changedProperties.get(impl) as Set<String>
        }
}
