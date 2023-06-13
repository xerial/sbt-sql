package xerial.sbt.sql

import sbt._
import sbt.Keys.sourceDirectory
import sbt.plugins.JvmPlugin

object SbtSQLDuckDB extends AutoPlugin {
  object autoImport extends SQL.Keys

  import autoImport._

  lazy val duckdbSettings = SQL.sqlSettings ++ Seq(
    sqlDir     := (Compile / sourceDirectory).value / "sql" / "duckdb",
    jdbcDriver := "org.duckdb.DuckDBDriver",
    // TODO support multiple files
    jdbcURL := "jdbc:duckdb:"
  )

  override def trigger         = noTrigger
  override def requires        = JvmPlugin
  override def projectSettings = duckdbSettings
}
