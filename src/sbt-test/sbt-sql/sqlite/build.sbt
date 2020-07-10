enablePlugins(SbtSQLJDBC)

name := "sbt-sql-test"
libraryDependencies ++= Seq("org.xerial" % "sqlite-jdbc" % "3.20.0")

// You can change SQL file folder. The default is src/main/sql
sqlDir := (sourceDirectory in Compile).value / "sql" / "sqlite"

// Configure your JDBC driver
jdbcDriver := "org.sqlite.JDBC"
jdbcURL := "jdbc:sqlite:sample.db"

def check(cond: Boolean): Unit =
{
  if (!cond) {
    sys.error(s"Validation failed")
  }
}

TaskKey[Unit]("check") := {
  val p1 = IO.read(target.value / "scala-2.12/src_managed/main/person.scala")
  check(p1.contains("case class person"))
  check(p1.contains("id: String"))
  check(p1.contains("name: String"))

  val p2 = IO.read(target.value / "scala-2.12/src_managed/main/person_opt.scala")
  check(p2.contains("case class person_opt"))
  check(p2.contains("id: String"))
  check(p2.contains("name: Option[String]"))

  val p3 = IO.read(target.value / "scala-2.12/src_managed/main/person_opt2.scala")
  check(p3.contains("case class person_opt2"))
  check(p3.contains("id: String"))
  check(p3.contains("name: Option[String]"))
}
