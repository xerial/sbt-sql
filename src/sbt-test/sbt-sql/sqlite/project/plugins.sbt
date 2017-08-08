addSbtPlugin("org.xerial.sbt" % "sbt-sql" % "0.8-SNAPSHOT")
// Add your jdbc driver dependency for checking the result schema
libraryDependencies ++= Seq(
  "org.xerial" % "sqlite-jdbc" % "3.20.0"
)
