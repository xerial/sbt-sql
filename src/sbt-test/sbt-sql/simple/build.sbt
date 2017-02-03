enablePlugins(SQL)

lazy val root = (project in file(".")).
  settings(
    name := "sbt-sql-test",
    version := "0.2-SNAPSHOT",
    jdbcUser := "<Your API KEY>"
)

