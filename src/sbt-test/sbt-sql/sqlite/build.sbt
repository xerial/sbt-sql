enablePlugins(SbtSQLJDBC)

name := "sbt-sql-test"
libraryDependencies ++= Seq(
  "org.xerial" % "sqlite-jdbc" % "3.39.4.0"
)

// You can change SQL file folder. The default is src/main/sql
sqlDir := (sourceDirectory in Compile).value / "sql" / "sqlite"

// Configure your JDBC driver
jdbcDriver := "org.sqlite.JDBC"
jdbcURL    := "jdbc:sqlite:sample.db"
