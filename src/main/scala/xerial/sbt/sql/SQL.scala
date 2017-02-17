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
    val jdbcURL      = taskKey[String]("JDBC connection URL. e.g., jdbc:presto://api-presto.treasuredata.com:443/td-presto")
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
    sqlDir := (sourceDirectory in Compile).value / "sql",
    generateSQLModel := {
      val config = JDBCConfig(jdbcDriver.value, jdbcURL.value, jdbcUser.value, jdbcPassword.value)
      val generator = new SQLModelClassGenerator(config, new SbtLogSupport(state.value.log)) //, state.value.log)
      generator.generate(
        GeneratorConfig(sqlDir.value,
          (managedSourceDirectories in Compile).value.head,
          (managedResourceDirectories in Compile).value.head
        )
      )
    },
    sqlModelClasses := generateSQLModel.value.map(_._1),
    sqlResources := generateSQLModel.value.map(_._2),
    sourceGenerators in Compile += sqlModelClasses.taskValue,
    resourceGenerators in Compile += sqlResources.taskValue,
    watchSources ++= (sqlDir.value ** "*.sql").get,
    jdbcUser := "",
    jdbcPassword := ""
  )

  lazy val prestoSettings = Seq(
    sqlDir := (sourceDirectory in Compile).value / "sql" / "presto",
    jdbcDriver := "com.facebook.presto.jdbc.PrestoDriver",
    jdbcURL := "jdbc:presto://(your presto server url)/(catalog name)"
  )

  lazy val tdPrestoSettings = prestoSettings ++ Seq(
    jdbcURL := {
      val host = credentials.value.collectFirst {
        case d: DirectCredentials if d.realm == "Treasure Data" =>
          d.host
      }.getOrElse("api-presto.treasuredata.com")
      s"jdbc:presto://${host}:443/td-presto"
    },
    jdbcUser := {
      val user = credentials.value.collectFirst {
        case d: DirectCredentials if d.realm == "Treasure Data" =>
          d.userName
      }
      user.orElse(sys.env.get("TD_API_KEY")).getOrElse("")
    }
  )

  override def trigger = allRequirements
  override def requires = JvmPlugin
  override def projectSettings = sqlSettings

}

