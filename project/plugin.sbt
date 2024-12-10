// Ignore binary incompatible errors for libraries using scala-xml.
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % "always"
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.12.2")
addSbtPlugin("com.github.sbt" % "sbt-pgp"      % "2.3.1")
addSbtPlugin("org.scalameta"  % "sbt-scalafmt" % "2.5.2")
addSbtPlugin("com.github.sbt" % "sbt-dynver"   % "5.1.0")

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
