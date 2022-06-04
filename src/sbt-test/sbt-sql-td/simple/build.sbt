lazy val root =
  project
    .in(file("."))
    .enablePlugins(SbtSQLTreasureData)
    .settings(
      name := "sbt-sql-test"
    )
