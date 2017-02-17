package xerial.sbt.sql

import sbt.{AutoPlugin, DirectCredentials}
import sbt.Keys.{credentials, sourceDirectory}
import sbt.plugins.JvmPlugin

/**
  *
  */
object JDBC extends AutoPlugin {
  object autoImport extends SQL.Keys
  import autoImport._

  override def trigger = allRequirements
  override def requires = JvmPlugin
  override def projectSettings = SQL.sqlSettings
}
