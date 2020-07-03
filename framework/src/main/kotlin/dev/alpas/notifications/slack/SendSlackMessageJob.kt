package dev.alpas.notifications.slack

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.awaitUnit
import com.github.kittinunf.fuel.core.extensions.jsonBody
import dev.alpas.Container
import dev.alpas.queue.job.Job

class SendSlackMessageJob(val message: SlackMessage, val webhook: String) : Job() {
    override suspend fun invoke(container: Container) {
        invoke()
    }

    suspend operator fun invoke() {
        Fuel.post(webhook)
            .jsonBody(message.asJson())
            .awaitUnit()
    }
}
