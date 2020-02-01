package dev.alpas.ozone

import dev.alpas.Application

abstract class Seeder {
    abstract fun run(app: Application)
}
