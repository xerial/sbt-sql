// Ignore binary incompatible errors for libraries using scala-xml.
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml"    % "always"
addSbtPlugin("com.github.sbt"                                     % "sbt-release"  % "1.1.0")
addSbtPlugin("org.xerial.sbt"                                     % "sbt-sonatype" % "3.9.20")
addSbtPlugin("com.github.sbt"                                     % "sbt-pgp"      % "2.2.1")
addSbtPlugin("org.scalameta"                                      % "sbt-scalafmt" % "2.5.0")

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
