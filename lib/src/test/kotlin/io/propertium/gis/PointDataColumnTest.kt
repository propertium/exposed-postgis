package io.propertium.gis

import io.propertium.gis.models.PointData
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class PointDataColumnTest {
    companion object {

        var db: Database? = null

        @BeforeAll
        @JvmStatic
        fun setupDatabase() {
            db = Database.connect(
                url = "jdbc:h2:mem:testdbgis;DB_CLOSE_DELAY=-1;",
                driver = "org.h2.Driver",
                user = "sa",
                password = ""
            )

            transaction {
                exec("CREATE ALIAS IF NOT EXISTS H2GIS_SPATIAL FOR 'org.h2gis.functions.factory.H2GISFunctions.load';")
                exec("CALL H2GIS_SPATIAL();")

                SchemaUtils.create(AttractionsTable)
                SchemaUtils.create(AttractionsTable)
            }

        }

        @AfterAll
        @JvmStatic
        fun tearDownDatabase() {
            transaction(db) {
                SchemaUtils.drop(AttractionsTable)
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
            AttractionsTable.insert {
                it[id] = 1
                it[name] = "Marques de Pombal"
//                it[location] = stGeomFromText(PointData(-9.150173727569582, 38.725295704673194, 4326))
                it.setPoint(location, PointData(-9.150173727569582, 38.725295704673194, 4326))
            }

            val queryResult = transaction {
                AttractionsTable
                    .selectAll()
                    .map { it[AttractionsTable.location] }
            }

            assertNotNull(queryResult)

        }
    }
}