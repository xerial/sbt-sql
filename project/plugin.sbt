addSbtPlugin("com.github.sbt" % "sbt-release"  % "1.0.15")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.13")
addSbtPlugin("com.github.sbt" % "sbt-pgp"      % "2.1.2")
addSbtPlugin("org.scalameta"  % "sbt-scalafmt" % "2.4.6")

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
