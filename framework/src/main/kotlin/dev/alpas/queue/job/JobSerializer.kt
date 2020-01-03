package dev.alpas.queue.job

import com.fasterxml.jackson.databind.ObjectMapper
import dev.alpas.JsonSerializer

interface JobSerializer {
    fun <T : Job> serialize(job: T): String
    fun deserialize(from: String): Job
}

internal class JobSerializerImpl : JobSerializer {
    override fun <T : Job> serialize(job: T): String {
        return JsonSerializer.serialize(JobWrapper(job), ObjectMapper.DefaultTyping.NON_FINAL)
    }

    override fun deserialize(from: String): Job {
        return JsonSerializer.deserialize<JobWrapper<Job>>(from, ObjectMapper.DefaultTyping.NON_FINAL).job
    }

    private class JobWrapper<T : Job>(val job: T) {}
}

