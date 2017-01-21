package xerial.sbt.sql

import sbt._
import sbt.Keys._
import sbt.plugins.JvmPlugin
/**
  *
  */
object SQL extends AutoPlugin {

  trait Keys {
    val sqlDir = settingKey[File]("A folder containing SQL files. e.g. src/main/sql")
    val jdbcDriver = settingKey[String]("JDBC driver class name. e.g., com.facebook.presto.jdbc.PrestoDriver")
    val jdbcURL = settingKey[String]("JDBC connection URL. e.g., jdbc:presto://api-presto.treasuredata.com:443/td-presto")
    val jdbcUser = settingKey[String]("JDBC user name")
    val jdbcPassword = settingKey[String]("JDBC password")
  }

  object autoImport extends Keys

  import autoImport._

  lazy val sqlSettings = Seq[Def.Setting[_]](
    sqlDir := (sourceDirectory in Compile).value / "sql"
  )

  lazy val prestoSetting = sqlSettings ++ Seq(
    jdbcDriver := "com.facebook.jdbc.PrestoDriver",
    jdbcURL := "jdbc:presto://api-presto.treasuredata.com:443/td-presto",
  )

  override def trigger = allRequirements
  override def requires = JvmPlugin
  override def projectSettings = sqlSettings

}
