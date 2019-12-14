addSbtPlugin("org.xerial.sbt" % "sbt-sql" % "0.9")
// Add your jdbc driver dependency for checking the result schema
libraryDependencies ++= Seq(
  "org.xerial" % "sqlite-jdbc" % "3.20.0"
)
