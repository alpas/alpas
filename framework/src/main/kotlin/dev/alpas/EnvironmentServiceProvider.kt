package dev.alpas

import io.github.cdimascio.dotenv.dotenv
import java.io.File
import java.nio.file.Paths

internal class EnvironmentServiceProvider : ServiceProvider {
    private val runMode by lazy {
        val mode = System.getenv(RUN_MODE) ?: System.getProperty(RUN_MODE) ?: "server"
        RunMode.valueOf(mode.toUpperCase())
    }

    override fun register(app: Application) {
        val envPath = envPath(app)
        val rootDir = envPath.parentDir.toURI().path

        System.setProperty(ROOT_DIR_KEY, rootDir)
        System.setProperty(SRC_DIR_KEY, Paths.get(rootDir, "src", "main", "kotlin").toUri().path)

        app.bufferDebugLog("${envPath.envFile} found at: ${envPath.parentDir.path}")
        app.bufferDebugLog("Root is at: $rootDir")
        val dotenv = dotenv {
            filename = envPath.envFile
            directory = rootDir
        }
        app.singleton(Environment(dotenv, rootDir, app.srcPackage, runMode))
    }

    private fun envPath(app: Application): EnvPath {
        return if (runMode.isConsole()) {
            EnvPath(File(System.getenv(ROOT_DIR_KEY)), ".env")
        } else {
            val basePath = app.entryClass.protectionDomain.codeSource.location.toURI()
            findEnvDir(File(basePath.path))
        }
    }

    private fun findEnvDir(at: File): EnvPath {
        if (File(at, ".env").exists()) {
            if (runMode == RunMode.TEST && File(at, ".env.testing").exists()) {
                return EnvPath(at, ".env.testing")
            }
            return EnvPath(at, ".env")
        }
        return findEnvDir(at.parentFile)
    }

    private data class EnvPath(val parentDir: File, val envFile: String)
}
