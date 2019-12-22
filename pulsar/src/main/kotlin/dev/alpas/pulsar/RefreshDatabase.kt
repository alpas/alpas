package dev.alpas.pulsar

import dev.alpas.Container
import dev.alpas.console.AlpasCommand
import dev.alpas.make

interface RefreshDatabase {
    fun refreshDatabase(container: Container) {
        container.make<AlpasCommand>().execute(arrayOf("db:refresh", "-q"))
    }
}
