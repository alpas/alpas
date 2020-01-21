package dev.alpas.tests.rules

import dev.alpas.validation.Max
import dev.alpas.validation.Min
import dev.alpas.validation.Required
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ValidationRulesTests {
    @Test
    fun `max rule test`() {
        Max(8).apply {
            assertFalse(check("name", "eight-eight-eight"))
            assertEquals("The 'name' must be at most 8 characters long.", error)
        }

        Max(8).apply {
            assertTrue(check("name", "eight"))
            assertThrows(UninitializedPropertyAccessException::class.java) {
                error
            }
        }

        Max(8).apply {
            assertTrue(check("name", "12345678"))
            assertThrows(UninitializedPropertyAccessException::class.java) {
                error
            }
        }

        Max(8) { attr, value -> "$attr value should not be $value" }.apply {
            check("name", "eight-eight-eight")
            assertEquals("name value should not be eight-eight-eight", error)
        }
    }

    @Test
    fun `min rule test`() {
        Min(8).apply {
            assertFalse(check("name", "eight"))
            assertEquals("The 'name' must be at least 8 characters long.", error)
        }


        Min(8).apply {
            assertTrue(check("name", "12345678"))
            assertThrows(UninitializedPropertyAccessException::class.java) {
                error
            }
        }

        Min(8).apply {
            assertTrue(check("name", "eight eight eight"))
            assertThrows(UninitializedPropertyAccessException::class.java) {
                error
            }
        }

        Min(8) { attr, value -> "$attr value should not be $value" }.apply {
            check("name", "eight")
            assertEquals("name value should not be eight", error)
        }
    }

    @Test
    fun `required rule test`() {
        Required().apply {
            assertFalse(check("firstname", null))
            assertEquals("The required field 'firstname' is missing, null, or empty.", error)
        }

        Required().apply {
            assertFalse(check("firstname", ""))
            assertEquals("The required field 'firstname' is missing, null, or empty.", error)
        }

        Required().apply {
            assertTrue(check("firstname", "jane"))
            assertThrows(UninitializedPropertyAccessException::class.java) {
                error
            }
        }

        Required() { attr, value -> "$attr value should not be $value" }.apply {
            check("firstname", null)
            assertEquals("firstname value should not be null", error)
        }
    }
}
