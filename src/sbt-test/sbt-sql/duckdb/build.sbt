enablePlugins(SbtSQLDuckDB)

name := "sbt-sql-duckdb-test"
scalaVersion := "3.3.6"

libraryDependencies ++= Seq(
  "org.wvlet.airframe" %% "airframe-codec" % "23.6.0"
)
