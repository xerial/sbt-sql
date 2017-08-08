lazy val root =
  (project in file(".")).settings(
    name := "sbt-sql-test"
  )


libraryDependencies ++= Seq(
  // For using presto-jdbc
  "org.xerial" % "sqlite-jdbc" % "3.20.0"
)

// You can change SQL file folder. The default is src/main/sql
sqlDir := (sourceDirectory in Compile).value / "sql" / "sqlite"

// Configure your JDBC driver
jdbcDriver := "org.sqlite.JDBC"
jdbcURL := "jdbc:sqlite:sample.db"
