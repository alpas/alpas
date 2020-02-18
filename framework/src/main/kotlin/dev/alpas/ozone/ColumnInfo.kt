package dev.alpas.ozone

import me.liuwj.ktorm.schema.Column

data class ColumnInfo(val col: Column<*>, internal var meta: ColumnMetadata?) {
    fun after(name: String) {
        meta = meta?.copy(after = name)
    }

    fun after(column: Column<*>) {
        meta = meta?.copy(after = column.name)
    }
}

internal data class ColumnKey(val type: String, val colNames: Set<String>, val name: String = "")

data class ColumnReferenceConstraint(val foreignKey: String, val tableToRefer: String, val columnToRefer: String = "id") {
    var onDelete: String? = null
        private set

    fun onDeleteCascade() {
        onDelete("cascade")
    }

    fun onDelete(whatToDo: String) {
        onDelete = whatToDo
    }
}
