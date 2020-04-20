package dev.alpas

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import me.liuwj.ktorm.jackson.KtormModule

class JsonSerializer private constructor() {
    companion object {
        val mapper: ObjectMapper = ObjectMapper().registerKotlinModule().registerModule(KtormModule())

        fun serialize(input: Any?, defaultTyping: ObjectMapper.DefaultTyping? = null): String {
            if (input == null) return ""

            return if (defaultTyping == null) {
                mapper.copy().deactivateDefaultTyping()
            } else {
                mapper.copy().activateDefaultTyping(LaissezFaireSubTypeValidator(), defaultTyping)
            }.writeValueAsString(input)
        }

        inline fun <reified T> deserialize(input: String, defaultTyping: ObjectMapper.DefaultTyping? = null): T {
            return if (defaultTyping == null) {
                mapper.copy().deactivateDefaultTyping()
            } else {
                mapper.copy().activateDefaultTyping(LaissezFaireSubTypeValidator(), defaultTyping)
            }.readValue(input)
        }
    }
}
