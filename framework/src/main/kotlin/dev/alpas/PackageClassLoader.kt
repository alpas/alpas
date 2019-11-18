package dev.alpas

import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import java.io.Closeable
import kotlin.reflect.KClass

class PackageClassLoader(val packageName: String) : Closeable {
    private val classGraph = ClassGraph().whitelistPackages(packageName).disableNestedJarScanning().scan()

    override fun close() {
        classGraph.close()
    }

    fun load(function: PackageClassLoader.() -> Unit) {
        this.function()
    }

    fun classesImplementing(interfaze: KClass<out Any>, binder: (instance: ClassInfo) -> Unit) {
        classGraph.getClassesImplementing(interfaze.java.name).forEach { instance ->
            if (instance.packageName.startsWith(packageName)) {
                binder(instance)
            }
        }
    }

    fun classesImplementing(interfaze: KClass<out Any>): List<ClassInfo> {
        return classGraph.getClassesImplementing(interfaze.java.name).filter { instance ->
            instance.packageName.startsWith(packageName)
        }.toList()
    }

    fun classesExtending(clazz: KClass<out Any>, binder: (instance: ClassInfo) -> Unit) {
        classGraph.getSubclasses(clazz.java.name).forEach { instance ->
            if (instance.packageName.startsWith(packageName)) {
                binder(instance)
            }
        }
    }

    fun classesExtending(clazz: KClass<out Any>): List<ClassInfo> {
        return classGraph.getSubclasses(clazz.java.name).filter { instance ->
            instance.packageName.startsWith(packageName)
        }.toList()
    }

    fun classOfName(className: String): ClassInfo? {
        return classGraph.getClassInfo(className)
    }
}
