package xerial.sbt.sql

import sbt.AutoPlugin

import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

object presto extends AutoPlugin {
  object autoImport extends SQL.Keys
  import autoImport._

  lazy val prestoSettings = SQL.sqlSettings ++ Seq(
    sqlDir := (sourceDirectory in Compile).value / "sql" / "presto",
    jdbcDriver := "io.prestosql.jdbc.PrestoDriver",
    jdbcURL := "jdbc:presto://(your presto server url)/(catalog name)"
  )

  override def trigger = allRequirements
  override def requires = JvmPlugin
  override def projectSettings = prestoSettings
}
