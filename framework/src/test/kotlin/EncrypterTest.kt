package dev.alpas.tests

import dev.alpas.JsonSerializer
import dev.alpas.base64Decoded
import dev.alpas.encryption.EncryptedData
import dev.alpas.encryption.Encrypter
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EncrypterTest {
    @Test
    fun `an encrypted string can be decrypted`() {
        val key = "abcdef123"
        val secret = Encrypter(key).encrypt("secret")
        assertNotNull(secret)
        assertEquals("secret", Encrypter(key).decrypt(secret!!))
    }

    @Test
    fun `an encrypted string cannot be decrypted when the key changes`() {
        val secret = Encrypter("abcdef123").encrypt("secret")
        assertNotNull(secret)
        assertNull(Encrypter("123abcdef").decrypt(secret!!))
    }

    @Test
    fun `encrypted string is base64encoded and serialized`() {
        val key = "123abcdef123"
        val secret = Encrypter(key).encrypt("secret")
        val decoded = secret?.base64Decoded()
        assertNotNull(decoded)
        val data = JsonSerializer.deserialize<EncryptedData>(decoded!!)
        assertNotNull(data.value)
    }
}
