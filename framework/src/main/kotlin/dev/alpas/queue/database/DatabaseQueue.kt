package dev.alpas.queue.database

import dev.alpas.ozone.forUpdate
import dev.alpas.ozone.selectFirst
import dev.alpas.queue.JobHolder
import dev.alpas.queue.Queue
import dev.alpas.queue.job.Job
import dev.alpas.queue.job.JobSerializer
import dev.alpas.stackTraceString
import kotlinx.coroutines.delay
import me.liuwj.ktorm.database.useTransaction
import me.liuwj.ktorm.dsl.*
import java.time.Duration
import java.time.Instant

class DatabaseQueue(
    private val defaultQueueName: String,
    private val serializer: JobSerializer,
    private val sleep: Duration = Duration.ofSeconds(3)
) : Queue {

    override fun <T : Job> enqueue(job: T, onQueue: String?) {
        pushToDatabase(serializer.serialize(job), onQueue, job.delayInSeconds)
    }

    override suspend fun dequeue(from: String?, timeout: Duration?): JobHolder? {
        val queue = from ?: defaultQueueName
        // get the next available job and reserve it
        val job = useTransaction {
            nextJob(queue)?.apply(this::reserveJob)
        }?.let {
            DatabaseJobHolder(serializer.deserialize(it.payload), it, this)
        }
        if (job == null) {
            delay((timeout ?: sleep).toMillis())
        }
        return job
    }

    internal fun markAsFailedJob(record: JobRecord, e: Exception) {
        FailedJobRecords.insert {
            it.connection to "database"
            it.queue to record.queue
            it.payload to record.payload
            it.exception to e.stackTraceString
        }
    }

    internal fun deleteJob(record: JobRecord) {
        JobRecords.delete { JobRecords.id eq record.id }
    }

    private fun reserveJob(record: JobRecord) {
        JobRecords.update {
            it.reservedAt to nextAvailableTime()
            it.tries to (record.tries + 1)
            where { it.id eq record.id }
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

    internal fun putBack(record: JobRecord, delayInSeconds: Long = 0, tries: Int = 0) {
        JobRecords.update {
            it.payload to record.payload
            it.queue to record.queue
            it.tries to tries
            it.availableAt to nextAvailableTime(delayInSeconds)
            where {
                it.id eq record.id
            }
        }
    }

    private fun pushToDatabase(payload: String, onQueue: String?, delayInSeconds: Long = 0, tries: Int = 0) {
        JobRecords.insert {
            it.payload to payload
            it.queue to (onQueue ?: defaultQueueName)
            it.tries to tries
            it.availableAt to nextAvailableTime(delayInSeconds)
            it.createdAt to Instant.now().epochSecond
        }
    }

    private fun nextAvailableTime(delayInSeconds: Long = 0) = Instant.now().plusSeconds(delayInSeconds).epochSecond

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
