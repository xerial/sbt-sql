addSbtPlugin("com.github.sbt" % "sbt-release"  % "1.1.0")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.13")
addSbtPlugin("com.jsuereth"   % "sbt-pgp"      % "2.1.1")
addSbtPlugin("org.scalameta"  % "sbt-scalafmt" % "2.4.6")

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
