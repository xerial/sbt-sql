package xerial.sbt.sql

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
    val jdbcUser     = taskKey[String]("JDBC user name")
    val jdbcPassword = taskKey[String]("JDBC password")

    val generateSQLModel = taskKey[Seq[(File, File)]]("create model classes from SQL files")
    val sqlModelClasses = taskKey[Seq[File]]("Generated SQL model classes")
    val sqlResources = taskKey[Seq[File]]("Generated SQL files")
  }

  object autoImport extends Keys

  import autoImport._

  // TODO split plugins for each jdbc drivers (sbt-sql-presto, sbt-sql-mysql, etc.)
  lazy val sqlSettings = Seq(
    sqlDir := (sourceDirectory in Compile).value / "sql" / "presto",
    generateSQLModel := {
      val config = JDBCConfig(jdbcDriver.value, jdbcURL.value, jdbcUser.value, jdbcPassword.value)
      val generator = new SQLModelClassGenerator(config) //, state.value.log)
      generator.generate(
        GeneratorConfig(sqlDir.value,
          (managedSourceDirectories in Compile).value.head,
          (managedResourceDirectories in Compile).value.head
        )
      )
    },
    sqlModelClasses := generateSQLModel.value.map(_._1),
    sqlResources := generateSQLModel.value.map(_._2),
    (sourceGenerators in Compile) += sqlModelClasses.taskValue,
    (resourceGenerators in Compile) += sqlResources.taskValue,
    unmanagedSourceDirectories in Compile += sqlDir.value,
    jdbcUser := "",
    jdbcPassword := ""
  )

  lazy val prestoSettings = sqlSettings ++ Seq(
    jdbcDriver := "com.facebook.presto.jdbc.PrestoDriver",
    jdbcURL := "jdbc:presto://api-presto.treasuredata.com:443/td-presto"
  )

  override def trigger = allRequirements
  override def requires = JvmPlugin
  override def projectSettings = prestoSettings

}

