package dev.alpas.queue.database

import dev.alpas.queue.Queue
import dev.alpas.queue.job.Job
import dev.alpas.queue.job.JobSerializer
import dev.alpas.stackTraceString
import me.liuwj.ktorm.database.DialectFeatureNotSupportedException
import me.liuwj.ktorm.database.useTransaction
import me.liuwj.ktorm.dsl.*
import me.liuwj.ktorm.expression.SelectExpression
import me.liuwj.ktorm.expression.UnionExpression
import mu.KotlinLogging
import java.time.Instant

class DatabaseQueue(private val defaultQueueName: String, private val serializer: JobSerializer) : Queue {
    private val logger by lazy { KotlinLogging.logger {} }

    override fun dequeue(from: String?, listener: (Job?) -> Unit) {
        val queue = from ?: defaultQueueName
        // get the next available job and reserve it
        useTransaction { nextJob(queue)?.apply(this::reserveJob) }
            ?.let { handleJob(it, listener) }
    }

    private fun handleJob(record: JobRecord, listener: (Job?) -> Unit) {
        val job = serializer.deserialize(record.payload)
        try {
            listener(job)
            deleteJob(record)
            logger.info { "Job ${record.id} successfully processed!" }
        } catch (e: Exception) {
            val tries = record.tries + 1
            logger.warn { "Job ${record.id} processing failed. Tries: $tries." }
            logger.error { e.stackTraceString }
            if (tries > job.retries) {
                logger.warn { "Job '${record.id}' has exceeded its retries count. Moving this job to the failed jobs queue." }
                markAsFailedJob(record, e)
                deleteJob(record)
            } else {
                JobRecords.update {
                    JobRecords.tries to tries
                    JobRecords.reservedAt to null
                    JobRecords.availableAt to epochNow(job.delayInSeconds)
                    where { JobRecords.id eq record.id }
                }
            }
        }
    }

    private fun markAsFailedJob(record: JobRecord, e: Exception) {
        FailedJobRecords.insert {
            FailedJobRecords.connection to "database"
            FailedJobRecords.queue to record.queue
            FailedJobRecords.payload to record.payload
            FailedJobRecords.exception to e.stackTraceString
        }
    }

    private fun deleteJob(record: JobRecord) {
        JobRecords.delete { JobRecords.id eq record.id }
    }

    private fun reserveJob(record: JobRecord) {
        JobRecords.update {
            JobRecords.reservedAt to epochNow()
            JobRecords.tries to (record.tries + 1)
            where { JobRecords.id eq record.id }
        }
    }

    private fun nextJob(queue: String): JobRecord? {
        return JobRecords.select().forUpdate()
            .where {
                JobRecords.queue.eq(queue) and JobRecords.reservedAt.isNull() and JobRecords.availableAt.lessEq(Instant.now().epochSecond)
            }
            .selectFirst()
            .map(this::makeJobRecord)
            .firstOrNull()
    }

    override fun <T : Job> enqueue(job: T, onQueue: String?) {
        JobRecords.insert {
            JobRecords.payload to serializer.serialize(job)
            JobRecords.queue to (onQueue ?: defaultQueueName)
            JobRecords.tries to 0
            JobRecords.availableAt to epochNow(job.delayInSeconds)
            JobRecords.createdAt to epochNow()
        }
    }

    private fun epochNow(delay: Long = 0) = Instant.now().plusSeconds(delay).epochSecond

    private fun makeJobRecord(row: QueryRowSet): JobRecord {
        return JobRecord {
            id = row[JobRecords.id]!!
            queue = row[JobRecords.queue]!!
            payload = row[JobRecords.payload]!!
            tries = row[JobRecords.tries]!!
            reservedAt = row[JobRecords.reservedAt]
            availableAt = row[JobRecords.availableAt]!!
            createdAt = row[JobRecords.createdAt]!!
        }
    }
}

fun Query.selectFirst() = apply {
    try {
        limit(0, 1).firstOrNull()
    } catch (e: DialectFeatureNotSupportedException) {
        firstOrNull()
    }
}

fun Query.forUpdate(): Query {
    val expr = this.expression

    val exprForUpdate = when (expr) {
        is SelectExpression -> expr.copy(extraProperties = expr.extraProperties + Pair("forUpdate", true))
        is UnionExpression -> expr.copy(extraProperties = expr.extraProperties + Pair("forUpdate", true))
    }

    return this.copy(expression = exprForUpdate)
}
