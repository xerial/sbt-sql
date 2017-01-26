sbt-sql
====

A sbt plugin for generating model classes from SQL query files in `src/main/sql`.


## Usage

**project/plugins.sbt**
```scala
addSbtPlugin("org.xerial.sbt" % "sbt-sql" % "0.1")
```

**src/main/sql/presto/sample/Nasdaq.sql**
```sql
select * from sample_datasets.nasdaq
where TD_TIME_RANGE(time, '${start:String}', '${end:String}')
```

From this SQL file, sbt-sql generates Scala model classes corresponding this SQL

SQL can contain variables `${...:(type name)}`, and sbt-sql generate a function to populated  
them: `Nasdaq.sql(start, end)`. 

### Generated Files 
**target/src_managed/main/test/Nasdaq.scala**
```scala
package sample
import java.sql.ResultSet

object Nasdaql {
  def path : String = "/sample/Nasdaq.sql"
  def originalSql : String = {
    scala.io.Source.fromInputStream(this.getClass.getResourceAsStream(path)).mkString
  }
  def apply(rs:ResultSet) : sql_tmpl = {
    new sql_tmpl(
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
 
