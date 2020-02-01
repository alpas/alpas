package dev.alpas.console

import dev.alpas.Environment
import dev.alpas.asGray
import dev.alpas.relativize
import dev.alpas.toPath
import java.io.File
import java.nio.file.Files

abstract class LinkResourcesCommand(
    name: String,
    private val env: Environment,
    private val dest: String,
    target: String
) :
    Command(
        name = name,
        help = "Link ${env.rootPath().relativize(dest).asGray()} to ${target.asGray()} directory"
    ) {

    protected abstract val docsUrl: String

    override fun run() {
        File(dest).apply {
            parentFile.mkdirs()
            val link = toPath()
            val targetRelativePath = env.storagePath().relativize(dest)
            val target = env.rootPath(targetRelativePath).toPath()
            Files.createSymbolicLink(link, target)

            withColors {
                echo(green("LINK CREATED 🙌"))
                echo("${dim(env.rootPath().relativize(link.toAbsolutePath()))} ↝ ${dim(env.rootPath().relativize(target.toAbsolutePath()))}")
                echo(yellow(docsUrl))
            }
        }
    }
}
