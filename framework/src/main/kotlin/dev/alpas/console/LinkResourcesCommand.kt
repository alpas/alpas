package dev.alpas.console

import dev.alpas.*
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
        help = "Link to ${target.asGray()} directory",
        epilog = "This command links ${env.rootPath().relativize(dest).asMagenta()} to ${target.asMagenta()} directory."
    ) {

    protected abstract val docsUrl: String

    override fun run() {
        File(dest).apply {
            parentFile.mkdirs()
            val link = toPath()
            val targetRelativePath = env.storagePath().relativize(dest)
            val target = env.rootPath(targetRelativePath).toPath()

            if (Files.exists(link)) {
                withColors {
                    echo(yellow("LINK ALREADY EXISTS. NO NEED TO RUN THIS COMMAND."))
                    echo(dim(env.rootPath().relativize(link.toAbsolutePath())))
                    echo(yellow(docsUrl))
                }
            } else {
                Files.createSymbolicLink(link, target)
                withColors {
                    echo("${green(" ✓")} ${brightGreen("Link Created")}")
                    echo(
                        "${dim(env.rootPath().relativize(link.toAbsolutePath()))} ↝ ${dim(
                            env.rootPath().relativize(target.toAbsolutePath())
                        )}"
                    )
                    echo(yellow(docsUrl))
                }
            }
        }
    }
}
