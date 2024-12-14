package io.propertium.gis.models

data class PointData(val x: Double, val y: Double, val srid: Int = 4326) {
    fun toWKT(): String = "POINT($x $y)"
}