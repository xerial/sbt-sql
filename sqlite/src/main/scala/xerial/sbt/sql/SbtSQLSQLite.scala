package xerial.sbt.sql

import sbt._
import sbt.Keys.sourceDirectory
import sbt.plugins.JvmPlugin

/** */
object SbtSQLSQLite extends AutoPlugin {

  object autoImport extends SQL.Keys

  import autoImport._

  lazy val sqliteSettings = SQL.sqlSettings ++ Seq(
    sqlDir     := (Compile / sourceDirectory).value / "sql" / "sqlite",
    jdbcDriver := "org.sqlite.JDBC",
    // TODO support multiple files
    jdbcURL := "jdbc:sqlite::memory::"
  )

  override def trigger = noTrigger

  override def requires        = JvmPlugin
  override def projectSettings = sqliteSettings
}
