enablePlugins(SbtSQLJDBC)

name := "sbt-sql-test"
libraryDependencies ++= Seq(
  "org.xerial" % "sqlite-jdbc" % "3.41.2.1"
)

// You can change SQL file folder. The default is src/main/sql
sqlDir := (Compile / sourceDirectory).value / "sql" / "sqlite"

// Configure your JDBC driver
jdbcDriver := "org.sqlite.JDBC"
jdbcURL    := "jdbc:sqlite:sample.db"
