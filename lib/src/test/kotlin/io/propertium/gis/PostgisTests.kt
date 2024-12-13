//package io.propertium.gis
//
//import org.jetbrains.exposed.sql.*
//import org.jetbrains.exposed.sql.transactions.transaction
//import org.junit.Assert.assertNotNull
//import org.junit.jupiter.api.AfterAll
//import org.junit.jupiter.api.Assertions.assertEquals
//import org.junit.jupiter.api.BeforeAll
//import org.junit.jupiter.api.Test
//import org.testcontainers.containers.PostgreSQLContainer
//import org.testcontainers.junit.jupiter.Container
//import org.testcontainers.junit.jupiter.Testcontainers
//import org.testcontainers.utility.DockerImageName
//
//
//@Testcontainers
//class PostgisTests {
//    companion object {
//        val postGisImage = DockerImageName.parse("postgis/postgis:15-3.5-alpine").asCompatibleSubstituteFor("postgres")
//
//        @Container
//        private val postgisContainer = PostgreSQLContainer(postGisImage).apply {
//            withDatabaseName("testdb")
//            withUsername("testuser")
//            withPassword("testpassword")
//        }
//
//        @BeforeAll
//        @JvmStatic
//        fun setupDatabase() {
//            postgisContainer.start()
//            val dataSource = org.postgresql.ds.PGSimpleDataSource().apply {
//                setUrl(postgisContainer.jdbcUrl)
//                user = postgisContainer.username
//                password = postgisContainer.password
//            }
//            Database.connect(dataSource)
//            transaction {
//                SchemaUtils.create(AttractionsTable)
//                SchemaUtils.create(BordersTable)
//            }
//        }
//
//        @AfterAll
//        @JvmStatic
//        fun tearDownDatabase() {
//            transaction {
//                SchemaUtils.drop(AttractionsTable)
//                SchemaUtils.drop(BordersTable)
//            }
//            postgisContainer.stop()
//        }
//    }
//
//    object AttractionsTable : Table("test_table") {
//        val id = integer("id").autoIncrement()
//        val name = varchar("name", 50)
//        val location = point("location", srid = 4326)
//        override val primaryKey = PrimaryKey(id)
//    }
//
//    object BordersTable : Table("test_table") {
//        val id = integer("id").autoIncrement()
//        val name = varchar("name", 50)
//        val borders = geometry("borders", srid = 4326)
//        override val primaryKey = PrimaryKey(id)
//    }
//
//    @Test
//    fun `test inserting and querying spatial data`() {
//        transaction {
//            AttractionsTable.insert {
//                it[id] = 1
//                it[name] = "Marques de Pombal"
//                it[location] = net.postgis.jdbc.geometry.Point(-9.150173727569582, 38.725295704673194)
//            }
//
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
//        }
//
//
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
//    }
//}