package dev.alpas.tests.validation

import dev.alpas.http.HttpCall
import dev.alpas.validation.*
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class ValidationGuardTest {
    @Test
    fun `check multiple rules for one attribute`(@RelaxedMockK call: HttpCall) {
        val errorBag = ErrorBag()
        every { call.paramList("name") } returns listOf("janeexampledoe")

        val guard = ValidationGuard().apply {
            rule(Max(10))
            rule(Min(5))
            this.call = call
        }

        guard.validate("name", errorBag)

        val allErrors = errorBag.all()
        assertEquals(1, allErrors.size)

        val maxError = allErrors.first()
        assertEquals("name", maxError.attribute)
        assertEquals("The 'name' must be at most 10 characters long.", maxError.message)
        assertEquals("janeexampledoe", (maxError.value as Collection<String>).first())
    }

    @Test
    fun `check multiple attributes`(@RelaxedMockK call: HttpCall) {
        val errorBag = ErrorBag()
        every { call.paramList("name") } returns listOf("janeexampledoe")
        every { call.paramList("email") } returns listOf("janedoe")

        val guard = ValidationGuard().apply { this.call = call }

        val nameRules = listOf(Max(10), Min(5))
        val emailRules = listOf(Email())

        guard.validate(mapOf("name" to nameRules, "email" to emailRules), errorBag)

        val allErrors = errorBag.all()
        assertEquals(2, allErrors.size)

        val maxError = allErrors.first()
        assertEquals("name", maxError.attribute)
        assertEquals("The 'name' must be at most 10 characters long.", maxError.message)
        assertEquals("janeexampledoe", (maxError.value as Collection<String>).first())

        val emailError = allErrors.last()
        assertEquals("email", emailError.attribute)
        assertEquals("'email' is not a valid email address.", emailError.message)
        assertEquals("janedoe", (emailError.value as Collection<String>).first())
    }

    @Test
    fun `check overriden rules`(@RelaxedMockK call: HttpCall) {
        val errorBag = ErrorBag()
        every { call.paramList("name") } returns listOf("janeexampledoe")
        every { call.paramList("lastname") } returns listOf("")
        every { call.paramList("email") } returns listOf("janedoe")

        val guard = object : ValidationGuard() {
            val nameRules = listOf(Max(10), Min(5))
            val lastNameRules = listOf(Required())
            val emailRules = listOf(Email())
            override fun rules(): Map<String, Iterable<Rule>> {
                return mapOf("name" to nameRules, "email" to emailRules, "lastname" to lastNameRules)
            }
        }.apply { this.call = call }


        guard.validate(errorBag)

        val allErrors = errorBag.all()
        assertEquals(3, allErrors.size)

        val maxError = allErrors.first()
        assertEquals("name", maxError.attribute)
        assertEquals("The 'name' must be at most 10 characters long.", maxError.message)
        assertEquals("janeexampledoe", (maxError.value as Collection<String>).first())

        val emailError = allErrors[1]
        assertEquals("email", emailError.attribute)
        assertEquals("'email' is not a valid email address.", emailError.message)
        assertEquals("janedoe", (emailError.value as Collection<String>).first())

        val lastnameError = allErrors.last()
        assertEquals("lastname", lastnameError.attribute)
        assertEquals("The required field 'lastname' is missing, null, or empty.", lastnameError.message)
        assertEquals("", (lastnameError.value as Collection<String>).first())
    }

    @Test
    fun `failfast should return on the first error`(@RelaxedMockK call: HttpCall) {
        val errorBag = ErrorBag()
        every { call.paramList("name") } returns listOf("janeexampledoe")
        every { call.paramList("lastname") } returns listOf("")
        every { call.paramList("email") } returns listOf("janedoe")

        val guard = object : ValidationGuard(true) {
            val nameRules = listOf(Max(10), Min(5))
            val lastNameRules = listOf(Required())
            val emailRules = listOf(Email())
            override fun rules(): Map<String, Iterable<Rule>> {
                return mapOf("name" to nameRules, "email" to emailRules, "lastname" to lastNameRules)
            }
        }.apply { this.call = call }


        guard.validate(errorBag)

        val allErrors = errorBag.all()
        assertEquals(1, allErrors.size)

        val maxError = allErrors.first()
        assertEquals("name", maxError.attribute)
    }
}
