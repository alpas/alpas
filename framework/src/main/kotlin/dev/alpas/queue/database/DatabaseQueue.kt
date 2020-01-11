package dev.alpas.queue.database

import dev.alpas.queue.JobHolder
import dev.alpas.queue.Queue
import dev.alpas.queue.job.Job
import dev.alpas.queue.job.JobSerializer
import dev.alpas.stackTraceString
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Duration
import java.time.Instant

class DatabaseQueue(
    private val defaultQueueName: String,
    private val serializer: JobSerializer,
    private val sleep: Duration = Duration.ofSeconds(3)
) : Queue {

    override fun <T : Job> enqueue(job: T, onQueue: String?) {
        transaction {
            pushToDatabase(serializer.serialize(job), onQueue, job.delayInSeconds)
        }
    }

    override fun dequeue(from: String?, timeout: Duration?): JobHolder? {
        val queue = from ?: defaultQueueName
        // get the next available job and reserve it
        val job = transaction {
            nextJob(queue)?.apply(::reserveJob)
        }?.let {
            DatabaseJobHolder(serializer.deserialize(it.payload), it, this)
        }
        if (job == null) {
            Thread.sleep((timeout ?: sleep).toMillis())
        }
        return job
    }

    internal fun markAsFailedJob(record: JobRecord, e: Exception) {
        FailedJobRecord.new {
            connection = "database"
            queue = record.queue
            payload = record.payload
            exception = e.stackTraceString
        }
    }

    internal fun deleteJob(record: JobRecord) {
        JobRecords.deleteWhere { JobRecords.id eq record.id }
    }

    private fun reserveJob(record: JobRecord) {
        JobRecords.update({ JobRecords.id eq record.id }) {
            it[reservedAt] = epochNow()
            it[tries] = record.tries++
        }
    }

    private fun nextJob(queue: String): JobRecord? {
        return JobRecord.find {
            (JobRecords.queue eq queue) and (JobRecords.reservedAt.isNull()) and (JobRecords.availableAt.lessEq(Instant.now().epochSecond))
        }.forUpdate().limit(1).firstOrNull()
    }

    internal fun pushToDatabase(payload: String, onQueue: String?, delayInSeconds: Long = 0, tries: Int = 0) {
        JobRecords.insert {
            it[JobRecords.payload] = payload
            it[JobRecords.queue] = (onQueue ?: defaultQueueName)
            it[JobRecords.tries] = tries
            it[JobRecords.availableAt] = epochNow(delayInSeconds)
            it[JobRecords.createdAt] = epochNow()
        }
    }

    private fun epochNow(delayInSeconds: Long = 0) = Instant.now().plusSeconds(delayInSeconds).epochSecond
}
