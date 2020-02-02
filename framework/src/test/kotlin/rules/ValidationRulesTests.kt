package dev.alpas.tests.rules

import dev.alpas.validation.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ValidationRulesTests {
    @Test
    fun `max rule test`() {
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

        Max(8).apply {
            assertFalse(check("name", "eight-eight-eight"))
            assertEquals("The 'name' must be at most 8 characters long.", error)
        }

        Max(8) { attr, value -> "$attr value should not be $value" }.apply {
            check("name", "eight-eight-eight")
            assertEquals("name value should not be eight-eight-eight", error)
        }
    }

    @Test
    fun `min rule test`() {
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

        Min(8).apply {
            assertFalse(check("name", "eight"))
            assertEquals("The 'name' must be at least 8 characters long.", error)
        }

        Min(8) { attr, value -> "$attr value should not be $value" }.apply {
            check("name", "eight")
            assertEquals("name value should not be eight", error)
        }
    }

    @Test
    fun `required rule test`() {
        Required().apply {
            assertTrue(check("firstname", "jane"))
            assertThrows(UninitializedPropertyAccessException::class.java) {
                error
            }
        }

        Required().apply {
            assertFalse(check("firstname", null))
            assertEquals("The required field 'firstname' is missing, null, or empty.", error)
        }

        Required().apply {
            assertFalse(check("firstname", ""))
            assertEquals("The required field 'firstname' is missing, null, or empty.", error)
        }

        Required() { attr, value -> "$attr value should not be $value" }.apply {
            check("firstname", null)
            assertEquals("firstname value should not be null", error)
        }
    }

    @Test
    fun `not-null rule test`() {
        NotNull().apply {
            assertTrue(check("lastname", ""))
            assertThrows(UninitializedPropertyAccessException::class.java) {
                error
            }
        }

        NotNull().apply {
            assertTrue(check("lastname", "doe"))
            assertThrows(UninitializedPropertyAccessException::class.java) {
                error
            }
        }

        NotNull().apply {
            assertFalse(check("lastname", null))
            assertEquals("The non null field 'lastname' is null.", error)
        }

        NotNull() { attr, value -> "$attr value should not be $value" }.apply {
            check("lastname", null)
            assertEquals("lastname value should not be null", error)
        }
    }

    @Test
    fun `must-be-integer rule test`() {
        MustBeInteger().apply {
            assertTrue(check("id", 28))
            assertThrows(UninitializedPropertyAccessException::class.java) {
                error
            }
        }

        MustBeInteger().apply {
            assertTrue(check("id", 9223372036854775807L))
            assertThrows(UninitializedPropertyAccessException::class.java) {
                error
            }
        }

        MustBeInteger().apply {
            assertFalse(check("id", null))
            assertEquals("The field 'id' must be an integer.", error)
        }

        MustBeInteger().apply {
            assertFalse(check("id", "string"))
            assertEquals("The field 'id' must be an integer.", error)
        }

        MustBeInteger() { attr, value -> "$attr value should not be $value" }.apply {
            check("id", "string")
            assertEquals("id value should not be string", error)
        }
    }

    @Test
    fun `must-be-string rule test`() {
        MustBeString().apply {
            assertTrue(check("address", "     "))
            assertThrows(UninitializedPropertyAccessException::class.java) {
                error
            }
        }

        MustBeString().apply {
            assertTrue(check("address", "test street"))
            assertThrows(UninitializedPropertyAccessException::class.java) {
                error
            }
        }

        MustBeString().apply {
            assertFalse(check("address", null))
            assertEquals("The field 'address' must be a string.", error)
        }

        MustBeString().apply {
            assertFalse(check("address", 29))
            assertEquals("The field 'address' must be a string.", error)
        }

        MustBeString() { attr, value -> "$attr value should not be $value" }.apply {
            check("address", 29)
            assertEquals("address value should not be 29", error)
        }
    }

    @Test
    fun `email rule test`() {
        Email().apply {
            assertTrue(check("email", "test@test.com"))
            assertThrows(UninitializedPropertyAccessException::class.java) {
                error
            }
        }

        Email().apply {
            assertTrue(check("email", "123@123"))
            assertThrows(UninitializedPropertyAccessException::class.java) {
                error
            }
        }

        Email().apply {
            assertFalse(check("email", null))
            assertEquals("'email' is not a valid email address.", error)
        }

        Email().apply {
            assertFalse(check("email", "     "))
            assertEquals("'email' is not a valid email address.", error)
        }

        Email().apply {
            assertFalse(check("email", "q@"))
            assertEquals("'email' is not a valid email address.", error)
        }

        Email().apply {
            assertFalse(check("email", "@"))
            assertEquals("'email' is not a valid email address.", error)
        }

        Email().apply {
            assertFalse(check("email", "@q"))
            assertEquals("'email' is not a valid email address.", error)
        }

        Email().apply {
            assertFalse(check("email", 2022020))
            assertEquals("'email' is not a valid email address.", error)
        }

        Email() { attr, value -> "$attr value should not be $value" }.apply {
            check("email", 2022020)
            assertEquals("email value should not be 2022020", error)
        }
    }
}
