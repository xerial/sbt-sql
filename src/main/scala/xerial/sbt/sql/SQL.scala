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
      //generator.generate(sqlDir.value)
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

