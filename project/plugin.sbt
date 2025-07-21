// Ignore binary incompatible errors for libraries using scala-xml.
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % "always"
addSbtPlugin("com.github.sbt" % "sbt-pgp"      % "2.3.1")
addSbtPlugin("org.scalameta"  % "sbt-scalafmt" % "2.5.5")
addSbtPlugin("com.github.sbt" % "sbt-dynver"   % "5.1.1")

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
