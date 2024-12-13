# extension-exposed-postgis

`extension-exposed-postgis` is a Kotlin library built on top of [Exposed](https://github.com/JetBrains/Exposed) to support PostGIS-enabled PostgreSQL databases. This library provides seamless and type-safe integration for spatial data manipulation.

## Features

- Support for PostGIS types like `POINT`, `GEOMETRY`, etc., in Exposed table schemas.
- Integration with PostGIS spatial functions such as `ST_Within`, `ST_TileEnvelope`, `ST_Intersects`, and more.
- Type-safe DSL for working with geographical queries.
- Compatible with Exposed's querying mechanism.
- Easy testing setup with [Testcontainers](https://www.testcontainers.org/) for PostGIS.

## Getting Started

To use `extension-exposed-postgis`, ensure your project has the required dependencies and that your PostgreSQL database is PostGIS-enabled.

### Prerequisites

1. **PostGIS-enabled PostgreSQL Database** with the necessary extensions installed.
2. **PostGIS JDBC Driver** for handling spatial data.
3. **Exposed SQL framework** is used as the base library.

### Dependency Configuration

Include the required dependencies in your Gradle build script:

```kotlin
dependencies {
    implementation("io.github.nikitok:exposed-postgis:0.4")
    implementation("net.postgis:postgis-jdbc:2023.1.0")
}

repositories {
    mavenCentral()
}
```

Replace `<version>` with your desired versions.

---

## Usage

### Table Definitions

Define your tables with PostGIS-supported data types:

```kotlin
object AttractionsTable : Table("test_table") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50)
    val location = point("location", srid = 4326)  // POINT data type with SRID
    override val primaryKey = PrimaryKey(id)
}

object BordersTable : Table("test_table") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50)
    val borders = geometry("borders", srid = 4326)  // GEOMETRY type
    override val primaryKey = PrimaryKey(id)
}
```

### Insert Data

You can insert spatial data using the `point()` and `geometry()` types, which map to PostGIS-supported fields:

```kotlin
transaction {
    AttractionsTable.insert {
        it[id] = 1
        it[name] = "Marques de Pombal"
        it[location] = net.postgis.jdbc.geometry.Point(-9.150173727569582, 38.725295704673194)
    }

    BordersTable.insert {
        it[id] = 1
        it[name] = "City Border"
        it[borders] = net.postgis.jdbc.geometry.Geometry("POLYGON ((...))")
    }
}
```

### Querying Data

Extend your queries with spatial functions for PostGIS operations:

#### Spatial Function Example: `ST_TileEnvelope`

```kotlin
BordersTable
    .selectAll()
    .where { BordersTable.borders.st_tileenvelope(TileEnv(x, y, z)) }
    .firstOrNull()
```

#### Spatial Function Example: `ST_Within`

```kotlin
val innerArea: Query = InnerAreaTable
    .select(InnerAreaTable.area)
    .where {
        InnerAreaTable.id.eq(UUID.fromString(areaID))
    }
BordersTable.borders.st_within(innerArea)
```

#### General Example: Querying Spatial Relationships

```kotlin
val queryResult = transaction {
    AttractionsTable
        .selectAll()
        .where { AttractionsTable.location.st_within(BordersTable.borders) }
        .firstOrNull()
}
```

### Testing with PostGIS

The library integrates well with [Testcontainers](https://www.testcontainers.org/), allowing you to set up a PostGIS-enabled PostgreSQL container for testing:

```kotlin
val postgisContainer = PostgreSQLContainer("postgis/postgis:15-3.5-alpine").apply {
    withDatabaseName("testdb")
    withUsername("testuser")
    withPassword("testpassword")
}
```

This makes testing spatial queries simple and reliable in isolated environments.

---

## Documentation

### Supported Data Types

| PostGIS Type   | Method              | Description                                   |
|----------------|---------------------|-----------------------------------------------|
| `POINT`        | `point()`           | Represents geospatial points (e.g., latitude, longitude). |
| `GEOMETRY`     | `geometry(type)`    | Generic geometry type for polygons, polygons, or multi-geometries. |

### Supported Spatial Functions

| Function           | Description                                           |
|--------------------|-------------------------------------------------------|
| `ST_Within`        | Checks if one geometry is completely within another.  |
| `ST_TileEnvelope`  | Creates envelope geometry for spatial tile queries.   |
| `ST_Intersects`    | Determines if two geometries intersect.               |

More spatial operations are supported; check the library documentation for a full list.

---

## Testing Example

Here’s an example of inserting and querying spatial data using `extension-exposed-postgis`:

```kotlin
@Test
fun `test inserting and querying spatial data`() {
    transaction {
        AttractionsTable.insert {
            it[id] = 1
            it[name] = "Marques de Pombal"
            it[location] = net.postgis.jdbc.geometry.Point(-9.150173727569582, 38.725295704673194)
        }

        val queryResult = AttractionsTable
            .selectAll()
            .where { AttractionsTable.location.st_within(BordersTable.borders) }
            .firstOrNull()

        assertNotNull(queryResult)
        assertEquals(1, queryResult!![AttractionsTable.id])
    }
}
```

---

## Contributing

Contributions are welcome! You can:

- Open issues for bug reports or feature requests.
- Submit pull requests for code contributions.
- Add tests to maintain compatibility and reliability.

### Guidelines

1. Write tests for any new feature or bug fix.
2. Ensure compatibility with the `Exposed` library and PostGIS features.

---

## Acknowledgments

Special thanks to:

- The creators of [Exposed](https://github.com/JetBrains/Exposed) for their fantastic SQL DSL.
- The [PostGIS](https://postgis.net/) team for enabling rich spatial capabilities.
- [Testcontainers](https://www.testcontainers.org/) for simplifying database testing.

---

## License

This project is available under the MIT License. See the LICENSE file for details.