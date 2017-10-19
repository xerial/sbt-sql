addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.6")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.0")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.0")
addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.0-RC12")
//addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.0")
//addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.1.0")
//addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.9.0")

//addSbtPlugin("com.eed3si9n" % "sbt-dirty-money" % "0.1.0")
scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
