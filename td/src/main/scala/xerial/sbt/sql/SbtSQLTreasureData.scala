package xerial.sbt.sql

import sbt.{AutoPlugin, DirectCredentials}
import sbt.plugins.JvmPlugin
import sbt.Keys._
import sbt._

object SbtSQLTreasureData extends AutoPlugin {

  object autoImport extends SQL.Keys

  import autoImport._

  lazy val prestoSettings = SQL.sqlSettings ++ Seq(
    sqlDir := (sourceDirectory in Compile).value / "sql" / "presto",
    jdbcDriver := "io.prestosql.jdbc.PrestoDriver",
    jdbcURL := {
      val host = credentials.value
        .collectFirst {
          case d: DirectCredentials if d.realm == "Treasure Data" =>
            d.host
        }.getOrElse("api-presto.treasuredata.com")
      s"jdbc:presto://${host}:443/td-presto?SSL=true"
    },
    jdbcUser := {
      val user = credentials.value.collectFirst {
        case d: DirectCredentials if d.realm == "Treasure Data" =>
          d.userName
      }
      user.orElse(sys.env.get("TD_API_KEY")).getOrElse("")
    },
    jdbcPassword := "dummy-password"
  )

  override def trigger = noTrigger

  override def requires        = JvmPlugin
  override def projectSettings = prestoSettings
}
