package xerial.sbt.sql

import sbt.Keys._
import sbt._
import sbt.nio.Keys.watchTriggers

/** */
object SQL {

  trait Keys {
    val sqlDir     = settingKey[File]("A folder containing SQL files. e.g. src/main/sql")
    val jdbcDriver = settingKey[String]("JDBC driver class name. e.g., com.facebook.presto.jdbc.PrestoDriver")
    // These tasks depend on external databases or environment variables, so they must not be cached in sbt 2
    @transient
    val jdbcURL = taskKey[String]("JDBC connection URL. e.g., jdbc:trino://api-presto.treasuredata.com:443/td-presto")
    @transient
    val jdbcUser = taskKey[String]("JDBC user name")
    @transient
    val jdbcPassword = taskKey[String]("JDBC password")
    @transient
    val generateSQLModel = taskKey[Seq[File]]("create model classes from SQL files")
    @transient
    val sqlModelClasses = taskKey[Seq[File]]("Generated SQL model classes")
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
    Compile / compile / watchTriggers += sqlDir.value.toGlob / ** / "*.sql",
    jdbcUser     := "",
    jdbcPassword := ""
  )
}
