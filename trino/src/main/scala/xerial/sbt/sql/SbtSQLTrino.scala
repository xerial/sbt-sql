package xerial.sbt.sql

import sbt.AutoPlugin

import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

object SbtSQLTrino extends AutoPlugin {

  object autoImport extends SQL.Keys

  import autoImport._

  lazy val trinoSettings = SQL.sqlSettings ++ Seq(
    sqlDir     := (sourceDirectory in Compile).value / "sql" / "trino",
    jdbcDriver := "io.trino.jdbc.TrinoDriver",
    jdbcURL    := "jdbc:trino://(your trino server url)/(catalog name)"
  )

  override def trigger = noTrigger

  override def requires        = JvmPlugin
  override def projectSettings = trinoSettings
}
