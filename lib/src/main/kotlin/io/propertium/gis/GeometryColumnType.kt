package io.propertium.gis

import net.postgis.jdbc.PGbox2d
import net.postgis.jdbc.PGgeometry
import net.postgis.jdbc.geometry.Geometry
import net.postgis.jdbc.geometry.GeometryCollection
import net.postgis.jdbc.geometry.MultiPolygon
import net.postgis.jdbc.geometry.Point
import org.jetbrains.exposed.sql.*


fun Table.geometry(name: String, srid: Int = 3857): Column<Geometry> = registerColumn(name, GeometryColumnType(srid))
infix fun ExpressionWithColumnType<*>.st_contains(box: Point) : Op<Boolean> = StContainsOp(this, box)
infix fun ExpressionWithColumnType<*>.st_within(query: Query) : Op<Boolean> = StWithin3(this, query)
data class TileEnv(val x:Int, val y:Int, val z:Int)
infix fun ExpressionWithColumnType<*>.st_tileenvelope(tile:TileEnv) : Op<Boolean> = StTileEnvelope(this, tile)
infix fun ExpressionWithColumnType<*>.withinGeo(box: PGbox2d) : Op<Boolean> = WithinOpGeo(this, box)

class GeometryColumnType(val srid: Int = 3857): ColumnType<Geometry>() {
    override fun sqlType() = "GEOMETRY(GEOMETRY, $srid)"

    override fun valueFromDB(value: Any): Geometry = when(value) {
        is PGgeometry -> {
            value.geometry
        }

        is GeometryCollection -> {
            value
        }

        else -> {
            value
        }
    } as Geometry

    override fun notNullValueToDB(value: Geometry): Any {
        if (value is Point) {
            if (value.srid == Point.UNKNOWN_SRID) value.srid = srid
            return PGgeometry(value)
        }
        if(value is GeometryCollection){
            if (value.srid == Geometry.UNKNOWN_SRID) value.srid = srid
            return PGgeometry(value)
        }
        if(value is MultiPolygon){
            if (value.srid == Geometry.UNKNOWN_SRID) value.srid = srid
            return PGgeometry(value)
        }
        return value
    }
}


private class StContainsOp(val expr1: Expression<*>, val box: Point) : Op<Boolean>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        with(queryBuilder) {
            + " ST_Contains(${(expr1 as Column).name}, ST_Transform(ST_SetSRID(ST_MakePoint(${box.x}, ${box.y}), 4326), 3857))"
        }
    }
}

private class StTileEnvelope(val expr1: Expression<*>, val tile: TileEnv) : Op<Boolean>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        with(queryBuilder) {
            + " ${(expr1 as Column).name} && ST_Transform(ST_TileEnvelope(${tile.z}, ${tile.x}, ${tile.y}, margin => 0.015625), 4326)"
        }
    }
}

private class WithinOpGeo(val expr1: Expression<*>, val box: PGbox2d) : Op<Boolean>() {
    //    override fun toSQL(queryBuilder: QueryBuilder) =
    //        "${expr1.toSQL(queryBuilder)} && ST_MakeEnvelope(${box.llb.x}, ${box.llb.y}, ${box.urt.x}, ${box.urt.y}, 4326)"

    //ST_MakeEnvelope
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        with(queryBuilder) {
            +expr1
            +" && ST_MakeEnvelope(${box.llb.x}, ${box.llb.y}, ${box.urt.x}, ${box.urt.y}, 4326)"
        }
    }
}

private class StWithin2(val expr1: Expression<*>, val expr2: Expression<*>) : Op<Boolean>() {
    //    o ST_Within(h.location, u.area)

    //ST_MakeEnvelope
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        with(queryBuilder) {
            +" ST_Within(${(expr1 as Column).name}, ${(expr2 as Column).name})"
        }
    }
}

private class StWithin3(val expr1: Expression<*>, val expr2: Query) : Op<Boolean>() {
    //    o ST_Within(h.location, u.area)

    //ST_MakeEnvelope
    override fun toQueryBuilder(queryBuilder: QueryBuilder) {
        val sql = expr2.prepareSQL(QueryBuilder(false))
        with(queryBuilder) {
            +" ST_Within(${(expr1 as Column).name}, (${sql}))"
        }
    }
}
