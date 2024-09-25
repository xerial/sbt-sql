enablePlugins(SbtSQLSQLite)

name    := "sqlite-basic-test"
jdbcURL := "jdbc:sqlite:sample.db"

val AIRFRAME_VERSION = "23.5.3"

libraryDependencies ++= Seq(
  "org.xerial"          % "sqlite-jdbc"    % "3.46.1.2",
  "org.wvlet.airframe" %% "airframe-codec" % AIRFRAME_VERSION,
  "org.wvlet.airframe" %% "airspec"        % AIRFRAME_VERSION % "test"
)

testFrameworks += new TestFramework("wvlet.airspec.Framework")
