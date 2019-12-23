package dev.alpas.queue.console

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import dev.alpas.Container
import dev.alpas.console.Command
import dev.alpas.console.GeneratorCommand
import dev.alpas.console.OutputFile
import dev.alpas.extensions.toPascalCase
import dev.alpas.make
import dev.alpas.ozone.console.MIGRATION_FILE_DATE_FORMAT
import dev.alpas.queue.Queue
import dev.alpas.queue.console.stubs.Stubs
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class QueueWorkCommand(private val container: Container) :
    Command(name = "queue:work", help = "Start a queue worker.") {
    private val queueName by argument(help = "Name of the queue").optional()
    override fun run() {
        withColors {
            if (queueName == null) {
                echo(green("Running default queue..."))
            } else {
                echo(green("Running '$queueName' queue..."))
            }
        }
        do {
            container.make<Queue>().dequeue(queueName, container.make()) {
                it?.invoke(container)
            }
        } while (true)
    }
}

class MakeJobCommand(srcPackage: String) : GeneratorCommand(srcPackage, name = "make:job", help = "Create a new job.") {
    override fun populateOutputFile(filename: String, actualname: String, vararg parentDirs: String): OutputFile {
        return OutputFile()
            .target(File(sourceOutputPath("jobs", *parentDirs), "${filename.toPascalCase()}.kt"))
            .packageName(makePackageName("jobs", *parentDirs))
            .stub(Stubs.jobStub())
    }

    override fun onCompleted(outputFile: OutputFile) {
        withColors {
            echo(green("JOB CREATED 🙌"))
            echo("${brightGreen(outputFile.target.name)}: ${dim(outputFile.target.path)}")
        }
    }
}

class QueueTablesCommand(srcPackage: String) :
    GeneratorCommand(
        srcPackage,
        name = "queue:tables",
        help = "Create all the required migrations for a database queue."
    ) {

    override val names = listOf("create_queue_jobs_tables")

    override fun populateOutputFile(filename: String, actualname: String, vararg parentDirs: String): OutputFile {
        val packageName = makePackageName("database", "migrations", *parentDirs)
        val outputPath = sourceOutputPath("database", "migrations", *parentDirs)
        val datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern(MIGRATION_FILE_DATE_FORMAT))

        return OutputFile()
            .target(File(outputPath, "${datePrefix}_$filename.kt"))
            .packageName(packageName)
            .className(filename)
            .stub(Stubs.queueTablesStub())
    }

    override fun onCompleted(outputFile: OutputFile) {
        withColors {
            echo(green("MIGRATIONS CREATED 🙌"))
            echo("${brightGreen(outputFile.target.name)}: ${dim(outputFile.target.path)}")
        }
    }
}

