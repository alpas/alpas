package dev.alpas

interface JsonSerializable {
    fun toJson(): String {
        return this.toJson(null)
    }

    fun toJson(wrapItWith: String?): String {
        return if (wrapItWith != null) {
            JsonSerializer.serialize(mapOf(wrapItWith to this))
        } else {
            JsonSerializer.serialize(this)
        }
    }
}

