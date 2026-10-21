enablePlugins(SbtSQLDuckDB)

name := "sbt-sql-duckdb-test"

libraryDependencies ++= Seq(
  "org.wvlet.airframe" %% "airframe-codec" % "23.6.0"
)

// The managed source directory differs between sbt 1 and sbt 2
TaskKey[Unit]("checkGenerated") := {
  val dir = (Compile / managedSourceDirectories).value.head
  for (f <- Seq("example/all_types.scala")) {
    assert((dir / f).exists, s"${dir / f} is not generated")
  }
}
