package io.propertium.gis

import io.propertium.gis.models.PointData
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.vendors.currentDialect
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import pointData
import stGeomFromText

class PostgisTests {
    companion object {

        var db: Database? = null

        @BeforeAll
        @JvmStatic
        fun setupDatabase() {
            db = Database.connect(
                url = "jdbc:h2:mem:testdbgis",
                driver = "org.h2.Driver",
                user = "sa",
                password = ""
            )
        }

        @AfterAll
        @JvmStatic
        fun tearDownDatabase() {
            transaction(db) {
//                SchemaUtils.drop(AttractionsTable)
//                SchemaUtils.drop(BordersTable)
            }
        }
    }

    object AttractionsTable : Table() {
        val id = integer("id").autoIncrement()
        val name = varchar("name", 50)
        val location = pointData("location", srid = 4326)
        override val primaryKey = PrimaryKey(id)
    }



    @Test
    fun `test inserting and querying spatial data`() {
        transaction(db) {

            exec("CREATE ALIAS IF NOT EXISTS H2GIS_SPATIAL FOR 'org.h2gis.functions.factory.H2GISFunctions.load';")
            exec("CALL H2GIS_SPATIAL();")

            SchemaUtils.create(AttractionsTable)
//            exec("""INSERT INTO ATTRACTIONS (ID, "name", LOCATION) VALUES (2, 'Marques de Pombal', ST_SetSRID(ST_MakePoint(-9.150173727569582, 38.725295704673194), 4326))""")
//            SchemaUtils.create(BordersTable)

            AttractionsTable.insert {
                it[id] = 1
                it[name] = "Marques de Pombal"
                it[location] = stGeomFromText(PointData(-9.150173727569582, 38.725295704673194, 4326))
            }

//            AttractionsTable.insert {
//                it[id] = 2
//                it[name] = "Saldana"
//                it[location] = net.postgis.jdbc.geometry.Point(-9.144766394195559, 38.733799268441906)
//            }
//
//            BordersTable.insert {
//                it[id] = 2
//                it[name] = "Lisboa"
//                it[borders] = net.postgis.jdbc.geometry.Point(-9.144766394195559, 38.733799268441906)
//            }
        }


//        val queryResult = transaction {
//            AttractionsTable
//                .selectAll()
//                .where { AttractionsTable.location.StWithin(AttractionsTable.location) }
//                .firstOrNull()
//        }
//
//
//        assertNotNull(queryResult)
//        assertEquals(1, queryResult!![AttractionsTable.id])
    }
}