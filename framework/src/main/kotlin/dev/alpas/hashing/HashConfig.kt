package dev.alpas.hashing

import dev.alpas.Config

open class HashConfig : Config {
    // max memory, in KB, to use for hashing
    open val memory = 1024
    // no of threads to use for hashing
    open val threads = 2
    // no of iterations to hash the string.
    open val iterations = 2
}
