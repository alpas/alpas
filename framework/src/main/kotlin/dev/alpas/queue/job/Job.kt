package dev.alpas.queue.job

import dev.alpas.Container
import dev.alpas.queue.database.JobRecord
import java.lang.Exception

open class Job {
    open val delayInSeconds: Long = 1
    open val tries: Int = 3
    open suspend operator fun invoke(container: Container) {}
    open fun onAttemptFailed(record: JobRecord, tries: Int): Boolean {
        // return true if this failed attempt is handled and should not be pushed to the database
        return false
    }

    open fun onAttemptFailed(record: JobRecord, tries: Int, exception: Exception): Boolean {
        return onAttemptFailed(record, tries)
    }

    open fun onJobFailed(record: JobRecord): Boolean {
        // return true if this failed job is handled and should not be pushed to the database
        return false
    }

    open fun onJobFailed(record: JobRecord, exception: Exception): Boolean {
        return onJobFailed(record)
    }
}
