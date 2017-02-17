lazy val root =
    (project in file(".")).settings(
        name := "sbt-sql-test",
        version := "0.1-SNAPSHOT",
        td.prestoSettings
    )
