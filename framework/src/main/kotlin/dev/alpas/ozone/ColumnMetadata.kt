package dev.alpas.ozone

data class ColumnMetadata(
    val size: Int? = null,
    val defaultValue: Any? = null,
    val unsigned: Boolean = false,
    val autoIncrement: Boolean = false,
    val nullable: Boolean = false,
    val unique: Boolean = false,
    val index: Boolean = true,
    val precision: Precision? = null,
    val useCurrentTimestamp: Boolean = false,
    internal val after: String? = null
)

data class Precision(val total: Int = 8, val places: Int = 2)
