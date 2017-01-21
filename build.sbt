import ReleaseTransformations._

val SCALA_VERSION = "2.10.6"

organization := "org.xerial.sbt"
organizationName := "Xerial project"
name := "sbt-sql"
organizationHomepage := Some(new URL("http://xerial.org/"))
description := "A sbt plugin for generating model classes from SQL files"
scalaVersion := SCALA_VERSION
publishMavenStyle := true
publishArtifact in Test := false

pomIncludeRepository := { _ => false }
sbtPlugin := true
parallelExecution := true
crossPaths := false
scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked", "-target:jvm-1.8")
scriptedBufferLog := false

libraryDependencies ++= Seq(
  "org.xerial" % "xerial-lens" % "3.2.3",
  "com.facebook.presto" % "presto-jdbc" % "0.163"
)

releaseTagName := {(version in ThisBuild).value}

releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      ReleaseStep(
        action = {state =>
          val extracted = Project extract state
          extracted.runAggregated(scriptedTests in Global in extracted.get(thisProjectRef), state)
        }
      ),
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      ReleaseStep(action = Command.process("publishSigned", _)),
      setNextVersion,
      commitNextVersion,
      ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
      pushChanges
    )

