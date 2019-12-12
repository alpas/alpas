package dev.alpas

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import me.liuwj.ktorm.jackson.KtormModule

class JsonSerializer {
    companion object {
        val mapper by lazy {
            ObjectMapper().registerKotlinModule().registerKtormModule()
        }

        fun serialize(input: Any?, dti: ObjectMapper.DefaultTyping? = null): String {
            return mapper.apply {
                if (dti != null) {
                    activateDefaultTyping(LaissezFaireSubTypeValidator(), dti)
                }
            }.writeValueAsString(input)
        }

        inline fun <reified T> deserialize(input: String, dti: ObjectMapper.DefaultTyping? = null): T {
            return mapper.apply {
                if (dti != null) {
                    activateDefaultTyping(LaissezFaireSubTypeValidator(), dti)
                }
            }.readValue(input)
        }
    }
}

fun ObjectMapper.registerKtormModule(): ObjectMapper = this.registerModule(KtormModule())
