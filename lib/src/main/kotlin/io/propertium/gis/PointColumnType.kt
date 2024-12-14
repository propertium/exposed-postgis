package io.propertium.gis


import net.postgis.jdbc.PGbox2d
import net.postgis.jdbc.PGgeometry
import net.postgis.jdbc.geometry.Point
import org.h2gis.functions.spatial.create.ST_MakePoint
import org.h2gis.functions.spatial.crs.ST_SetSRID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.jetbrains.exposed.sql.vendors.H2Dialect
import org.jetbrains.exposed.sql.vendors.currentDialect
import org.postgresql.util.PGobject
import java.sql.ResultSet

fun Table.point(name: String, srid: Int = 4326): Column<Point> = registerColumn(name, PointColumnType(srid))
infix fun ExpressionWithColumnType<*>.StWithin(box: Expression<*>) : Op<Boolean> = StWithin(this, box)
fun ExpressionWithColumnType<*>.inLocation(box: Point, boxSrid:Int, columnSrid: Int): Op<Boolean> = Within(this, box, boxSrid, columnSrid)
fun ExpressionWithColumnType<*>.inEnvelope(box: PGbox2d, boxSrid:Int, columnSrid: Int) : Op<Boolean> = MakeEnvelope(this, box, boxSrid, columnSrid)
fun ExpressionWithColumnType<*>.intersects(points: Array<Point>, boxSrid:Int, columnSrid: Int) : Op<Boolean> = MakeInrersects(this, points, boxSrid, columnSrid)


private class PointColumnType(val srid: Int = 4326): org.jetbrains.exposed.sql.ColumnType<net.postgis.jdbc.geometry.Point>() {
    override fun sqlType() = "GEOMETRY(POINT, $srid)"
    override fun valueFromDB(value: Any): Point? {
        return value as? Point
    }

    override fun notNullValueToDB(value: Point): Any {
        return value
    }
}

private class MakeInrersects(val expr1: Expression<*>, val points: Array<Point>, val boxSrid:Int = 4326, val columnSrid: Int = 4326) : Op<Boolean>() {

    //ST_MakeEnvelope
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        with(queryBuilder) {

            val points = points.map { "${it.x} ${it.y}" }.joinToString(",")
            val envelope = "ST_GeomFromText('POLYGON(( ${points} ))')"

            val transformedEnvelope = if (boxSrid != columnSrid) {
                val envelopeSource = "ST_SetSRID(${envelope}, ${boxSrid})"
                "ST_Transform($envelopeSource, $columnSrid)"
            } else {
                envelope
            }

            +" ST_Intersects(${(expr1 as Column).name}, ${transformedEnvelope})"
        }
    }
}


private class MakeEnvelope(val expr1: Expression<*>, val box: PGbox2d, val boxSrid:Int = 4326, val columnSrid: Int = 4326) : Op<Boolean>() {
    //    override fun toSQL(queryBuilder: QueryBuilder) =
    //        "${expr1.toSQL(queryBuilder)} && ST_MakeEnvelope(${box.llb.x}, ${box.llb.y}, ${box.urt.x}, ${box.urt.y}, 4326)"

    //ST_MakeEnvelope
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        with(queryBuilder) {
            val envelope = "ST_MakeEnvelope(${box.llb.x}, ${box.llb.y}, ${box.urt.x}, ${box.urt.y}, $boxSrid)"

            val transformedEnvelope = if (boxSrid != columnSrid) {
                "ST_Transform($envelope, $columnSrid)"
            } else {
                envelope
            }

            +expr1
            +" && $transformedEnvelope"
        }
    }
}

private class Within(val expr1: Expression<*>, val point: Point, val boxSrid:Int = 4326, val columnSrid: Int = 4326) : Op<Boolean>() {
    //    override fun toSQL(queryBuilder: QueryBuilder) =
    //        "${expr1.toSQL(queryBuilder)} && ST_MakeEnvelope(${box.llb.x}, ${box.llb.y}, ${box.urt.x}, ${box.urt.y}, 4326)"

    //ST_MakeEnvelope
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        with(queryBuilder) {
            val transformedPoint = if (boxSrid != columnSrid) {
                "ST_Transform(ST_SetSRID(ST_MakePoint(${point.x}, ${point.y}), $boxSrid), $columnSrid)"
            } else {
                "ST_SetSRID(ST_MakePoint(${point.x}, ${point.y}), $boxSrid)"
            }

            // Выражение генерации SQL
            +" ST_Within($transformedPoint, ${(expr1 as Column).name})"
        }
    }
}

private class StWithin(val expr1: Expression<*>, val expr2: Expression<*>) : Op<Boolean>() {
    //    o ST_Within(h.location, u.area)

    //ST_MakeEnvelope
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        with(queryBuilder) {
            +" ST_Within(${(expr1 as Column).name}, ${(expr2 as Column).name})"
        }
    }
}
