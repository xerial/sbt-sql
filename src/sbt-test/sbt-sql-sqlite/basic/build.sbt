enablePlugins(SbtSQLSQLite)

name := "sqlite-basic-test"
jdbcURL := "jdbc:sqlite:sample.db"

val AIRFRAME_VERSION = "20.6.2"

libraryDependencies ++= Seq(
  "org.xerial"          % "sqlite-jdbc"    % "3.32.3.3",
  "org.wvlet.airframe" %% "airframe-codec" % AIRFRAME_VERSION,
  "org.wvlet.airframe" %% "airspec"        % AIRFRAME_VERSION % "test"
)

testFrameworks += new TestFramework("wvlet.airspec.Framework")
