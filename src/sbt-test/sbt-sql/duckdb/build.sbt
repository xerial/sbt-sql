enablePlugins(SbtSQLDuckDB)

name := "sbt-sql-duckdb-test"

libraryDependencies ++= Seq(
  "org.wvlet.airframe" %% "airframe-codec" % "23.6.0"
)
