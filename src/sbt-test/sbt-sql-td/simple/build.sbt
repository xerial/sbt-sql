lazy val root =
  project
    .in(file("."))
    .enablePlugins(SbtSQLTreasureData)
    .settings(
      name := "sbt-sql-test"
    )

// The managed source directory differs between sbt 1 and sbt 2
TaskKey[Unit]("checkGenerated") := {
  val dir = (Compile / managedSourceDirectories).value.head
  for (f <- Seq("access_log.scala")) {
    assert((dir / f).exists, s"${dir / f} is not generated")
  }
}
