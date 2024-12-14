import io.propertium.gis.models.PointData
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.CustomFunction
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.intLiteral
import org.jetbrains.exposed.sql.stringLiteral
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.io.WKTReader
import org.locationtech.jts.io.WKTWriter

fun Table.pointData(name: String, srid: Int = 4326)  = registerColumn(name, PointDataColumnType(srid))
fun stGeomFromText(point: PointData): Expression<PointData> =
    CustomFunction("ST_GeomFromText", PointDataColumnType(point.srid), stringLiteral(point.toWKT()), intLiteral(point.srid))


private class PointDataColumnType(val srid: Int = 4326) : ColumnType<PointData>() {
    private val geometryFactory = GeometryFactory()
    private val wktReader = WKTReader(geometryFactory)
    private val wktWriter = WKTWriter()

    override fun sqlType(): String {
        return "GEOMETRY(POINT, $srid)"
    }

    override fun valueFromDB(value: Any): PointData? {
        return null
    }

    override fun notNullValueToDB(value: PointData): Any {
        throw UnsupportedOperationException("Please use stGeomFromText(PointData(...))")
    }
}