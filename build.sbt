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

pomIncludeRepository := {_ => false}
sbtPlugin := true
parallelExecution := true
crossPaths := false
scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked")
ScriptedPlugin.scriptedSettings
scriptedBufferLog := false

libraryDependencies ++= Seq(
  "org.xerial" % "xerial-lens" % "3.2.3",
  // TODO support multiple db types
  "com.facebook.presto" % "presto-jdbc" % "0.163",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

releaseTagName := {(version in ThisBuild).value}

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  ReleaseStep(
    action = {state =>
      val extracted = Project.extract(state)
      extracted.runAggregated(scriptedTests in Global in extracted.get(thisProjectRef), state)
    }
  ),
  setReleaseVersion,
  ReleaseStep(action = Command.process("bumpPluginVersion", _)),
  commitReleaseVersion,
  tagRelease,
  ReleaseStep(action = Command.process("publishSigned", _)),
  setNextVersion,
  ReleaseStep(action = Command.process("bumpPluginVersion", _)),
  commitNextVersion,
  ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
  pushChanges
)

resourceGenerators in Compile += Def.task {
  val buildProp = (resourceManaged in Compile).value / "org" / "xerial" / "sbt" / "sbt-sql" / "build.properties"
  val buildRev = Process("git" :: "rev-parse" :: "HEAD" :: Nil).!!.trim
  val buildTime = file("src/main/scala/xerial/sbt/sql/SQLModelClassGenerator.scala").lastModified()
  val contents = s"name=$name\nversion=${version.value}\nbuild_revision=$buildRev\nbuild_time=$buildTime"
  IO.write(buildProp, contents)
  Seq(buildProp)
}.taskValue


commands += Command.command("bumpPluginVersion") {state =>
  val extracted = Project.extract(state)
  val newVersion = extracted.get(version in ThisBuild)
  val pluginSbt = file("src/sbt-test/sbt-sql") ** "project" ** "plugins.sbt"

  for (f <- pluginSbt.get) {
    state.log.info(s"update sbt-sql plugin version in ${f}")
    val updated = (for (line <- IO.readLines(f)) yield {
      line.replaceAll("""(.+\"sbt-sql\" % \")([^"]+)("\))""", s"$$1${newVersion}$$3")
    })
    IO.writeLines(f, updated)
  }
  state
}
