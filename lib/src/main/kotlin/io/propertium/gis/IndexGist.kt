package io.propertium.gis

import org.jetbrains.exposed.sql.Column

fun <T> Column<T>.indexGist(customIndexName: String? = null, isUnique: Boolean = false): Column<T> =
    apply { table.index(null, false, this, indexType = "gist") }