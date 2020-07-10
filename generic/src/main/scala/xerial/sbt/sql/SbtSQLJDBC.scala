package xerial.sbt.sql

import sbt.AutoPlugin
import sbt.plugins.JvmPlugin

/**
  */
object SbtSQLJDBC extends AutoPlugin {

  object autoImport extends SQL.Keys

  override def trigger = noTrigger

  override def requires = JvmPlugin

  override def projectSettings = SQL.sqlSettings
}
