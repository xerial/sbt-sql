package xerial.sbt.sql

import java.sql.{Connection, DriverManager, JDBCType, ResultSet}

import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

/**
  *
  */
object SQL extends AutoPlugin {

  trait Keys {
    val sqlDir       = settingKey[File]("A folder containing SQL files. e.g. src/main/sql")
    val jdbcDriver   = settingKey[String]("JDBC driver class name. e.g., com.facebook.presto.jdbc.PrestoDriver")
    val jdbcURL      = settingKey[String]("JDBC connection URL. e.g., jdbc:presto://api-presto.treasuredata.com:443/td-presto")
    val jdbcUser     = settingKey[String]("JDBC user name")
    val jdbcPassword = settingKey[String]("JDBC password")

    val generateSQLModel = taskKey[Seq[File]]("create model classes from SQL files")
  }

  object autoImport extends Keys

  import autoImport._

  lazy val sqlSettings = Seq[Def.Setting[_]](
    sqlDir := (sourceDirectory in Compile).value / "sql",
    generateSQLModel := {
      val generated = Seq.newBuilder[File]
      val config = JDBCConfig(jdbcDriver.value, jdbcURL.value, jdbcUser.value, jdbcPassword.value)
      val generator = new SQLModelClassGenerator(config) //, state.value.log)
      generator.generate(sqlDir.value)
      generated.result()
    }
  )

  lazy val prestoSetting = sqlSettings ++ Seq(
    jdbcDriver := "com.facebook.jdbc.PrestoDriver",
    jdbcURL := "jdbc:presto://api-presto.treasuredata.com:443/td-presto"
  )

  override def trigger = allRequirements
  override def requires = JvmPlugin
  override def projectSettings = sqlSettings

}

private[sql] case class JDBCConfig(
  driver: String,
  url: String,
  user: String,
  password: String
)

case class Schema(columns: Seq[Column])
case class Column(name: String, sqlType: java.sql.JDBCType)

class SQLModelClassGenerator(config: JDBCConfig) extends xerial.core.log.Logger {

  private def withResource[R <: AutoCloseable, U](r: R)(body: R => U): U = {
    try {
      body(r)
    }
    finally {
      r.close()
    }
  }

  private def wrapWithLimit0(sql: String) = {
    s"""SELECT * FROM (
       |${sql}
       |)
       |LIMIT 0""".stripMargin
  }

  private def withConnection[U](body: Connection => U) : U = {
    Class.forName(config.driver)
    withResource(DriverManager.getConnection(config.url, config.user, config.password)) {conn =>
      body(conn)
    }
  }

  private def submitQuery[U](conn:Connection, sql: String)(body: ResultSet => U): U = {
      withResource(conn.createStatement()) {stmt =>
        info(s"running sql:\n${sql}")
        withResource(stmt.executeQuery(sql)) {rs =>
          body(rs)
        }
      }
  }

  def checkResultSchema(sql: String): Schema = {
    withConnection {conn =>
      submitQuery(conn, sql) {rs =>
        val m = rs.getMetaData
        val cols = m.getColumnCount
        val colTypes = (1 to cols).map {i =>
          val name = m.getColumnName(i)
          val tpe = m.getColumnType(i)
          val jdbcType = JDBCType.valueOf(tpe)
          Column(name, jdbcType)
        }
        Schema(colTypes.toIndexedSeq)
      }
    }
  }

  def generate(sqlDir:File) = {
    // Submit queries using multi-threads to minimize the waiting time
    for (sqlFile <- (sqlDir ** "*.sql").get.par) {
      info(s"Processing ${sqlFile}")
      val sql = IO.read(sqlFile)
      val limit0 = wrapWithLimit0(sql)
      val schema = checkResultSchema(limit0)
      info(schema)
      schemaToClass(sqlFile, sqlDir, schema)
    }
  }

  def schemaToClass(origFile: File, baseDir: File, schema: Schema): String = {
    val packageName = origFile.relativeTo(baseDir).map {f =>
      f.getParent.replaceAll("""[\\/]""", ".")
    }.getOrElse("")
    val name = origFile.getName.replaceAll("\\.sql$", "")


    val code =
      s"""
         |package ${packageName}
         |
         |case class ${name}(
         |
         |)
        """.stripMargin

    info(code)
    code
  }

}