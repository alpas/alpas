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

class Environment(
    private val dotenv: Dotenv,
    val rootDir: String,
    val entryPackage: String,
    val runMode: RunMode
) {
    internal val entries = dotenv.entries()
    val inTestMode by lazy { runMode.isTest() }
    val inConsoleMode by lazy { runMode.isConsole() }

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

    fun check(key: String, against: List<String>): Boolean {
        return against.contains(this(key)?.toLowerCase())
    }

    val isProduction by lazy { invoke("APP_LEVEL").isOneOf("prod", "production", "live") }
    val isLocal by lazy { invoke("APP_LEVEL").isOneOf("dev", "debug", "local") }
    val isDev = isLocal
    val storagePath: String = Paths.get(rootDir, "storage").toUri().path
    var supportsSession = false
        internal set
}
