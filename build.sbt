import ReleaseTransformations._

val SCALA_VERSION = "2.10.6"
val PRESTO_VERSION = "0.163"

ScriptedPlugin.scriptedSettings
scriptedBufferLog := false

val buildSettings = Seq(
  organization := "org.xerial.sbt",
  organizationName := "Xerial project",
  organizationHomepage := Some(new URL("http://xerial.org/")),
  description := "A sbt plugin for generating model classes from SQL files",
  scalaVersion := SCALA_VERSION,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  sbtPlugin := true,
  parallelExecution := true,
  crossPaths := false,
  scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked"),
  libraryDependencies ++= Seq(
    "org.xerial" % "xerial-lens" % "3.2.3",
    "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    "com.facebook.presto" % "presto-jdbc" % PRESTO_VERSION % "test"
  )
)

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

lazy val root : Project = Project(id="sbt-sql-root", base=file(".")).settings(
  buildSettings,
  publish := {},
  publishLocal := {},
  publishArtifact := false,
  test := {},
  releaseTagName := {(version in ThisBuild).value},
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
).aggregate(base, generic, presto)

lazy val base : Project = Project(id="sbt-sql-base", base= file("base")).settings(
  buildSettings,
  resourceGenerators in Compile += Def.task {
    val buildProp = (resourceManaged in Compile).value / "org" / "xerial" / "sbt" / "sbt-sql" / "build.properties"
    val buildRev = Process("git" :: "rev-parse" :: "HEAD" :: Nil).!!.trim
    val buildTime = ((sourceDirectory in Compile).value / "xerial/sbt/sql/SQLModelClassGenerator.scala").lastModified()
    val contents = s"name=$name\nversion=${version.value}\nbuild_revision=$buildRev\nbuild_time=$buildTime"
    IO.write(buildProp, contents)
    Seq(buildProp)
  }.taskValue
)

lazy val generic : Project = Project(id = "sbt-sql", base = file("generic")).settings(
  buildSettings,
  description := " A sbt plugin for generating model classes from SQL files",
  libraryDependencies ++= Seq(
  )
).dependsOn(base)

lazy val presto : Project = Project(id = "sbt-sql-presto", base = file("presto")).settings(
  buildSettings,
  description := " A sbt plugin for generating model classes from Presto SQL files",
  libraryDependencies ++= Seq(
    "com.facebook.presto" % "presto-jdbc" % PRESTO_VERSION
  )
).dependsOn(base)
