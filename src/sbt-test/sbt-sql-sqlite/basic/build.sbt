enablePlugins(SbtSQLSQLite)

name    := "sqlite-basic-test"
jdbcURL := "jdbc:sqlite:sample.db"

val AIRFRAME_VERSION = "23.5.3"

libraryDependencies ++= Seq(
  "org.xerial"          % "sqlite-jdbc"    % "3.54.1.0",
  "org.wvlet.airframe" %% "airframe-codec" % AIRFRAME_VERSION,
  "org.wvlet.airframe" %% "airspec"        % AIRFRAME_VERSION % "test"
)

testFrameworks += new TestFramework("wvlet.airspec.Framework")

// The managed source directory differs between sbt 1 and sbt 2
TaskKey[Unit]("checkGenerated") := {
  val dir = (Compile / managedSourceDirectories).value.head
  for (
    f <- Seq(
      "example/person.scala",
      "example/person_opt.scala",
      "example/person_opt2.scala",
      "example/person_by_id.scala"
    )
  ) {
    assert((dir / f).exists, s"${dir / f} is not generated")
  }
}
