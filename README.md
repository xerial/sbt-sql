sbt-sql 
====

A sbt plugin for generating model classes from SQL query files in `src/main/sql`.

## Why you need sbt-sql?

 - Integrate the power of SQL and Scala
     - If you write an SQL, it creates a Scala class to read the SQL result.
 - Type safety
     - No longer need to write a code like `ResultSet.getColumn("id")` etc. 
     - Editors such as IntelliJ can show the SQL result parameter names and types.
 - Reuse your SQL as a template
     - You can embed parameters in your SQL with automatically generated Scala functions.

## Usage

**project/plugins.sbt**

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.xerial.sbt/sbt-sql/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.xerial.sbt/sbt-sql)

```scala
addSbtPlugin("org.xerial.sbt" % "sbt-sql" % "0.1")
```

**src/main/sql/presto/sample/Nasdaq.sql**
```sql
select * from sample_datasets.nasdaq
where TD_TIME_RANGE(time, '${start:String}', '${end:String}')
```

From this SQL file, sbt-sql generates Scala model classes and utility methods.

* SQL can contain variables `${(variable name):(type)}`, and sbt-sql generates a function to populate them, such as `Nasdaq.sql(start, end)`. So the SQL file with template variables can be called as if it were a function in Scala.

For now sbt-sql works with [Treasure Data](http://www.treasuredata.com/) Presto. Set TD_API_KEY environment variable or
set jdbcUser property:
```
jdbcUser := <Your API Key>
```

### Generated Files 
**target/src_managed/main/sample/Nasdaq.scala**
```scala
package sample
import java.sql.ResultSet

object Nasdaq {
  def path : String = "/sample/Nasdaq.sql"
  def originalSql : String = {
    scala.io.Source.fromInputStream(this.getClass.getResourceAsStream(path)).mkString
  }
  def apply(rs:ResultSet) : Nasdaq = {
    new Nasdaq(
      rs.getString(1),
      rs.getDouble(2),
      rs.getLong(3),
      rs.getDouble(4),
      rs.getDouble(5),
      rs.getDouble(6),
      rs.getLong(7)
    )
  }
  def sql(start:String, end:String) : String = {
    var rendered = originalSql
    val params = Seq("start", "end")
    val args = Seq(start, end)
    for((p, arg) <- params.zip(args)) {
       rendered = rendered.replaceAll("\\$\\{" + p + "\\}", arg.toString)
    }
    rendered
  }
}

class Nasdaq(
  val symbol:String,
  val open:Double,
  val volume:Long,
  val high:Double,
  val low:Double,
  val close:Double,
  val time:Long
) {
  ...
}
``` 
 
