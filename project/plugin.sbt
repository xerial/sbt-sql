addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.12")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.8.1")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "2.0.0")

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
