package xerial.sbt.sql

import sbt.Keys._
import sbt.{given, *}
import sbt.internal.io.Source

/** */
object SQL {

  trait Keys {
    val sqlDir     = settingKey[File]("A folder containing SQL files. e.g. src/main/sql")
    val jdbcDriver = settingKey[String]("JDBC driver class name. e.g., com.facebook.presto.jdbc.PrestoDriver")
    val jdbcURL  = taskKey[String]("JDBC connection URL. e.g., jdbc:trino://api-presto.treasuredata.com:443/td-presto")
    val jdbcUser = taskKey[String]("JDBC user name")
    val jdbcPassword     = taskKey[String]("JDBC password")
    val generateSQLModel = taskKey[Seq[File]]("create model classes from SQL files")
    val sqlModelClasses  = taskKey[Seq[File]]("Generated SQL model classes")
  }

  object autoImport extends Keys

  import autoImport._

  // TODO split plugins for each jdbc drivers (mysqlSettings, prestoSettings, etc.)
  lazy val sqlSettings = Seq(
    sqlDir := (Compile / sourceDirectory).value / "sql",
    generateSQLModel := {
      val config    = JDBCConfig(jdbcDriver.value, jdbcURL.value, jdbcUser.value, jdbcPassword.value)
      val generator = new SQLModelClassGenerator(config)
      generator.generate(
        GeneratorConfig(sqlDir.value, (Compile / managedSourceDirectories).value.head)
      )
    },
    sqlModelClasses := generateSQLModel.value,
    Compile / sourceGenerators += sqlModelClasses.taskValue,
    watchSources += new Source(
      sqlDir.value,
      new NameFilter {
        override def accept(name: String): Boolean = {
          name.endsWith(".sql")
        }
      },
      NothingFilter
    ),
    jdbcUser     := "",
    jdbcPassword := ""
  )
}
