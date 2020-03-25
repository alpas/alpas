@file:Suppress("unused")

package dev.alpas

import io.github.cdimascio.dotenv.Dotenv
import java.nio.file.Paths

enum class RunMode {
    TEST, CONSOLE, SERVER;

    fun isConsole() = this == CONSOLE
    fun isTest() = this == TEST
}

const val ROOT_DIR_KEY = "alpas_root_dir"
const val SRC_DIR_KEY = "alpas_src_dir"
const val RUN_MODE = "alpas_run_mode"
const val STORAGE_PATH = "alpas_storage_path"
const val ROOT_PATH = "alpas_root_path"
const val STORAGE_TEMPLATES_PATH = "alpas_storage_templates_path"
val RESOURCES_DIRS = arrayOf("src", "main", "resources")

open class Environment(
    private val dotenv: Dotenv,
    private val rootPath: String,
    val entryPackage: String,
    val runMode: RunMode
) {
    val entries = dotenv.entries().map { entry -> entry.key to entry.value }.toMap()
    val inTestMode = runMode.isTest()
    val inConsoleMode = runMode.isConsole()
    val isProduction = invoke("APP_LEVEL").isOneOf("prod", "production", "live")
    val isLocal = invoke("APP_LEVEL").isOneOf("dev", "debug", "local")
    val isDev = isLocal
    val storagePath = rootPath("storage")
    var supportsSession = false
        internal set

    init {
        System.setProperty(ROOT_PATH, rootPath)
        System.setProperty(STORAGE_PATH, storagePath)
        System.setProperty(STORAGE_PATH, storagePath("templates"))
    }

    operator fun invoke(key: String): String? {
        return dotenv[key]
    }

    operator fun invoke(key: String, defaultValue: () -> String): String {
        return dotenv[key] ?: defaultValue()
    }

    operator fun invoke(key: String, defaultValue: () -> Int): Int {
        return dotenv[key]?.toInt() ?: defaultValue()
    }

    operator fun invoke(key: String, defaultValue: Int): Int {
        return dotenv[key]?.toInt() ?: defaultValue
    }

    operator fun invoke(key: String, defaultValue: Boolean): Boolean {
        return dotenv[key]?.toBoolean() ?: defaultValue
    }

    operator fun invoke(key: String, defaultValue: String): String {
        return dotenv[key] ?: defaultValue
    }

    fun rootPath(vararg paths: String): String {
        return if (paths.isEmpty()) {
            rootPath
        } else Paths.get(rootPath, *paths).toAbsolutePath().toString()
    }

    fun storagePath(vararg paths: String): String {
        return if (paths.isEmpty()) {
            storagePath
        } else Paths.get(storagePath, *paths).toAbsolutePath().toString()
    }
}
