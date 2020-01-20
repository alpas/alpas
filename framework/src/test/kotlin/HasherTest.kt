package dev.alpas.tests

import dev.alpas.base64Decoded
import dev.alpas.hashing.HashConfig
import dev.alpas.hashing.Hasher
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HasherTest {
    private val hasher = Hasher(TestHashConfig())
    @Test
    fun `a string is hashed`() {
        val hash = hasher.hash("password")
        assertNotEquals("password", hash)
    }

    @Test
    fun `a hashed string is encoded by default`() {
        val hash = hasher.hash("password")
        assertFalse(hash.startsWith("\$argon2i"))
        val decoded = hash.base64Decoded()
        assertTrue(decoded.startsWith("\$argon2i"))
    }

    @Test
    fun `a hashed string can be set to be not encoded`() {
        val hash = hasher.hash("password", false)
        assertTrue(hash.startsWith("\$argon2i"))
        assertThrows(IllegalArgumentException::class.java) {
            hash.base64Decoded()
        }
    }

    @Test
    fun `a hashed value and non-hashed value can be verified`() {
        val hash = hasher.hash("password")
        assertTrue(hasher.verify("password", hash))
        assertFalse(hasher.verify("secret", hash))
    }

    @Test
    fun `an encoded hashed value cannot be verified without decoding`() {
        val hash = hasher.hash("password")
        assertFalse(hasher.verify("password", hash, false))
        assertFalse(hasher.verify("secret", hash, false))
    }
}

private class TestHashConfig : HashConfig() {
    override val iterations = 1
}
