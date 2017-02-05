lazy val root = (project in file(".")).
  settings(
    name := "sbt-sql-test",
    version := "0.2-SNAPSHOT",
    jdbcUser := sys.env("TD_API_KEY")
)
