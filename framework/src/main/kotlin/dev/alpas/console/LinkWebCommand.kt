package dev.alpas.console

import dev.alpas.AppConfig
import dev.alpas.Environment
import dev.alpas.asGray
import dev.alpas.relativize
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class LinkWebCommand(private val env: Environment, private val config: AppConfig) :
    Command(
        name = "link:web",
        help = "Link ${env.rootDir.relativize(config.storageWebDirectory).asGray()} to ${"resources/web".asGray()} directory"
    ) {
    override fun run() {
        File(config.storageWebDirectory).apply {
            parentFile.mkdirs()
            val link = toPath()
            val targetRelativePath = env.storagePath.relativize(config.storageWebDirectory)
            val target = Paths.get(env.rootDir, targetRelativePath)
            Files.createSymbolicLink(link, target)
            withColors {
                echo(green("LINK CREATED 🙌"))
                echo("${dim(env.rootDir.relativize(link.toAbsolutePath()))} ↝ ${dim(env.rootDir.relativize(target.toAbsolutePath()))}")
            }
        }
    }
}
