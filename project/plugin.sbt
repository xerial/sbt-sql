addSbtPlugin("com.github.gseitz" % "sbt-release"  % "1.0.12")
addSbtPlugin("org.xerial.sbt"    % "sbt-sonatype" % "3.9.13")
addSbtPlugin("com.jsuereth"      % "sbt-pgp"      % "2.0.2")
addSbtPlugin("org.scalameta"     % "sbt-scalafmt" % "2.4.6")

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
