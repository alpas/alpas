package dev.alpas

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

class JsonSerializer {
    companion object {
        val mapper by lazy {
            ObjectMapper().registerKotlinModule()
        }

        fun serialize(input: Any?, dti: ObjectMapper.DefaultTyping? = null): String {
            return mapper.apply {
                if (dti != null) {
                    enableDefaultTyping(dti)
                }
            }.writeValueAsString(input)
        }

        inline fun <reified T> deserialize(input: String, dti: ObjectMapper.DefaultTyping? = null): T {
            return mapper.apply {
                if (dti != null) {
                    enableDefaultTyping(dti)
                }
            }.readValue(input)
        }
    }
}
