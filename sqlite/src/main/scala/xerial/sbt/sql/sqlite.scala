package xerial.sbt.sql

import sbt._
import sbt.Keys.sourceDirectory
import sbt.plugins.JvmPlugin

/**
  *
  */
object sqlite extends AutoPlugin {
  object autoImport extends SQL.Keys
  import autoImport._

  lazy val sqliteSettings = SQL.sqlSettings ++ Seq(
    sqlDir := (sourceDirectory in Compile).value / "sql" / "sqlite",
    jdbcDriver := "org.sqlite.JDBC",
    // TODO support multiple files
    jdbcURL := "jdbc:sqlite::memory::"
  )

  override def trigger = allRequirements
  override def requires = JvmPlugin
  override def projectSettings = sqliteSettings
}
