package io.propertium.gis

import io.propertium.gis.models.PointData
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.CustomFunction
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.intLiteral
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.jetbrains.exposed.sql.stringLiteral
import org.jetbrains.exposed.sql.vendors.PostgreSQLDialect
import org.jetbrains.exposed.sql.vendors.currentDialect
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.io.WKTReader
import org.locationtech.jts.io.WKTWriter

fun Table.pointData(name: String, srid: Int = 4326) = registerColumn(name, PointDataColumnType(srid))
fun PointData.toWKT(): String = "POINT($x $y)"
fun InsertStatement<Number>.setPoint(column: Column<PointData>, value: PointData) {
    // ST_SetSRID(ST_GeomFromText('POINT(-9.150173727569582 38.725295704673194)'), 4326)
    // ST_GeomFromText('POINT(-9.150173727569582 38.725295704673194)')
    this[column] = CustomFunction("ST_GeomFromText", PointDataColumnType(value.srid),
        stringLiteral(value.toWKT()), intLiteral(value.srid)
    )
}

private class PointDataColumnType(val srid: Int = 4326) : ColumnType<PointData>() {
    private val geometryFactory = GeometryFactory()
    private val wktReader = WKTReader(geometryFactory)
    private val wktWriter = WKTWriter()

    override fun sqlType(): String {
        return "GEOMETRY(POINT, $srid)"
    }

    override fun valueFromDB(value: Any): PointData? {
        return when (value) {
            is Point ->
                PointData(value.x, value.y, value.srid)
            is net.postgis.jdbc.geometry.Point ->
                PointData(value.x, value.y, value.srid)
            else ->
                throw UnsupportedOperationException("Please dialect ${currentDialect.name}")
        }
    }

    override fun notNullValueToDB(value: PointData): Any {
        return when (val dialect = currentDialect) {
            is PostgreSQLDialect -> {
                val point = net.postgis.jdbc.geometry.Point(value.x, value.y)
                point.srid = value.srid
                point
            }
            else ->
                throw UnsupportedOperationException("Please use stGeomFromText(PointData(...))")
        }
    }

    override fun parameterMarker(value: PointData?): String {
        return super.parameterMarker(value)
    }

    override fun setParameter(
        stmt: PreparedStatementApi,
        index: Int,
        value: Any?
    ) {
        super.setParameter(stmt, index, value)
    }
}