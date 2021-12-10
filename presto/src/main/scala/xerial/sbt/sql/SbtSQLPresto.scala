package xerial.sbt.sql

import sbt.AutoPlugin

import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

object SbtSQLPresto extends AutoPlugin {

  object autoImport extends SQL.Keys

  import autoImport._

  lazy val prestoSettings = SQL.sqlSettings ++ Seq(
    sqlDir := (Compile / sourceDirectory).value / "sql" / "presto",
    jdbcDriver := "io.prestosql.jdbc.PrestoDriver",
    jdbcURL := "jdbc:presto://(your presto server url)/(catalog name)"
  )

  override def trigger = noTrigger

  override def requires        = JvmPlugin
  override def projectSettings = prestoSettings
}
