sbt-sql
====

A sbt plugin for generating model classes from SQL query files in `src/main/sql`.

## Why you need sbt-sql?

 - Integrate the power of SQL and Scala
     - If you write an SQL, it creates a Scala class to read the SQL result.
 - Type safety
     - No longer need to write a code like `ResultSet.getColumn("id")` etc.
     - Editors such as IntelliJ can show the SQL result parameter names and types.
     - For example, if you rename a column name in SQL from `id` to `ID`, the code using `id` will be shown as compilation error. Without sbt-sql, it will be a run-time exception, such as `Unknown column "id"`!.
 - Reuse your SQL as a template
     - You can embed parameters in your SQL with automatically generated Scala functions.

## Usage

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.xerial.sbt/sbt-sql/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.xerial.sbt/sbt-sql)

sbt-sql supports only sbt 1.3.x or higher.

**project/plugins.sbt**
```scala
// For Presto
addSbtPlugin("org.xerial.sbt" % "sbt-sql-presto" % "(version)")

// For SQLite (available since 0.7.0)
addSbtPlugin("org.xerial.sbt" % "sbt-sql-sqlite" % "(version)")

// For Treasure Data Presto
addSbtPlugin("org.xerial.sbt" % "sbt-sql-td" % "(version)")

// For Generic JDBC drivers
addSbtPlugin("org.xerial.sbt" % "sbt-sql" % "(version)")
// Add your jdbc driver dependency for checking the result schema
libraryDependencies ++= Seq(
   // Add your jdbc driver here
)
```

**build.sbt**

This is an example of using a custom JDBC driver:

```scala
enablePlugins(SbtSQLJDBC)

// Add your JDBC driver to the dependency
// For using presto-jdbc
libraryDependencies ++= Seq(
  "org.wvlet.airframe" % "airframe-codec" % "20.6.2", // Necessary for mapping JDBC ResultSets to model classes
  "io.prestosql.presto" % "presto-jdbc" % "332"
 )

// You can change SQL file folder. The default is src/main/sql
// sqlDir := (sourceDirectory in Compile).value / "sql"

// Configure your JDBC driver
jdbcDriver := "io.prestosql.jdbc.PrestoDriver"
jdbcURL := "(jdbc url e.g., jdbc:presto://.... )"
jdbcUser := "(jdbc user name)"
jdbcPassword := "(jdbc password)"
```

### sbt-sql-sqlite

`sbt-sql-sqlite` plugin uses `src/main/sql/sqlite` as the SQL file directory. Configure `jdbcURL` and `jdbcUser` properties:
```scala
enablePlugins(SbtSQLSQLite)

jdbcURL := "jdbc:sqlite:(sqlite db file path)"
```

### sbt-sql-presto

`sbt-sql-presto` plugin uses `src/main/sql/presto` as the SQL file directory. Configure `jdbcURL` and `jdbcUser` properties:

```scala
enablePlugins(SbtSQLPresto)

jdbcURL := "jdbc:presto://api-presto.treasuredata.com:443/td-presto"
jdbcUser := "presto user name"
```

### sbt-sql-td (Treasure Data Presto)

To use [Treasure Data](http://www.treasuredata.com/) Presto, set TD_API_KEY environment variable.
`jdbcUser` will be set to this value.

Alternatively you can set TD_API_KEY in your sbt credential:

**$HOME/.sbt/1.0/td.sbt**
```
credentials +=
  Credentials("Treasure Data", "api-presto.treasuredata.com", "(your TD API KEY)", "")
```

```scala
enablePlugins(SbtSQLTreasureData)
```

## Writing SQL

**src/main/sql/presto/sample/nasdaq.sql**
```sql
@(start:Long, end:Long)
select * from sample_datasets.nasdaq
where time between ${start} and ${end}
```

From this SQL file, sbt-sql generates Scala model classes and several utility methods.

* SQL file can contain template variables `${(Scala expression)}`.
To define user input variables, use `@(name:type, ...)`. sbt-sql generates a function to populate them, such as `Nasdaq.select(start = xxxxx, end = yyyyy)`. The variable can have a default value, e.g., `@(x:String="hello")`.

### Template Variable Examples

- Embed a String value
```sql
@(symbol:String)
select * from sample_datasets.nasdaq
where symbol = '${symbol}'
```

- Embed an input table name as a variable with the default value `sample_datasets.nasdaq`:
```sql
@(table:SQL="sample_datasets.nasdaq")
select * from ${table}
```
SQL type can be used for embedding an SQL expression as a String.

### Import statement

You can use your own type for populating SQL templates by importing your class as follows:
```
@import your.own.class
```

### Using Option[X] types

To generate case classes with Option[X] parameters, add `__optional` suffix to the target column names.  

```
select a as a__optional // Option[X] will be used  
from ...
```

```scala
@optional(a, b)
select a, b, c // Option[A], Option[B], C 
```

### Generated Files
**target/src_managed/main/sample/nasdaq.scala**
```scala
package sample

object nasdaq {
  def path : String = "/sample/nasdaq.sql"

  def sql(start:Long, end:Long) : String = {
    s""""select * from sample_dataest.nasdaq
where time between ${start} and ${end}
    """
  }

  def select(start:Long, end:Long)(implicit conn:java.sql.Connection): Seq[nasdaq] = ...
  def selectWith(sql:String)(implicit conn:java.sql.Connection): Seq[nasdaq] = ...
}

case class nasdaq(
  symbol: String,
  open: Double,
  volume: Long,
  high: Double,
  low: Double,
  close: Double,
  time: Long
)
```

