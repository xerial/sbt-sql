enablePlugins(SbtSQLSQLite)

name := "sqlite-basic-test"
jdbcURL := "jdbc:sqlite:sample.db"

libraryDependencies ++= Seq(
  "org.xerial" % "sqlite-jdbc" % "3.32.3",
  "org.wvlet.airframe" %% "airframe-codec" % "20.6.2",
  "org.wvlet.airframe" %% "airframe-control" % "20.6.2"
)
