package dev.alpas.ozone

import me.liuwj.ktorm.schema.Column

internal data class ColumnInfo(val col: Column<*>, internal var meta: ColumnMetadata?)
internal data class ColumnKey(val type: String, val colNames: Set<String>, val name: String = "")
data class ColumnReferenceConstraint(
    val foreignKey: String,
    val tableToRefer: String,
    val columnToRefer: String = "id"
) {
    var onDelete: String? = null
        private set

    fun onDeleteCascade() {
        onDelete("cascade")
    }

    fun onDelete(whatToDo: String) {
        onDelete = whatToDo
    }
}
