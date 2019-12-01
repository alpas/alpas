package dev.alpas.hashing

import de.mkammerer.argon2.Argon2Factory
import dev.alpas.base64Decoded
import dev.alpas.base64Encoded

class Hasher(private val config: HashConfig) {
    private val argon by lazy { Argon2Factory.create() }
    // hash of the given value
    fun hash(value: CharArray, encode: Boolean = true): String {
        try {
            return argon.hash(config.iterations, config.memory, config.threads, value).let {
                if (encode) it.base64Encoded() else it
            }
        } finally {
            argon.wipeArray(value)
        }
    }

    fun hash(rawString: String, encode: Boolean = true) = hash(rawString.toCharArray(), encode)

    // verify the given value against the hashed value
    fun verify(value: String?, hashedValue: String?, decode: Boolean = true): Boolean {
        if (value == null || hashedValue == null) return false
        val realHash = if (decode) hashedValue.base64Decoded() else hashedValue
        return argon.verify(realHash, value)
    }
}
