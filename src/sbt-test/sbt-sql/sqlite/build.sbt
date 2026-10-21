enablePlugins(SbtSQLJDBC)

name := "sbt-sql-test"

// You can change SQL file folder. The default is src/main/sql
sqlDir := (Compile / sourceDirectory).value / "sql" / "sqlite"

// Configure your JDBC driver
jdbcDriver := "org.sqlite.JDBC"
jdbcURL    := "jdbc:sqlite:sample.db"

// The managed source directory differs between sbt 1 and sbt 2
TaskKey[Unit]("checkGenerated") := {
  val dir = (Compile / managedSourceDirectories).value.head
  for (f <- Seq("person.scala")) {
    assert((dir / f).exists, s"${dir / f} is not generated")
  }
}
