import ReleaseTransformations._

val PRESTO_VERSION = "333"
val SCALA_PARSER_COMBINATOR_VERSION = "1.1.2"

val SCALA_2_12 = "2.12.11"
ThisBuild / scalaVersion := SCALA_2_12

Global / onChangedBuildSource := ReloadOnSourceChanges

val buildSettings = Seq(
  organization := "org.xerial.sbt",
  sonatypeProfileName := "org.xerial",
  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  homepage := Some(url("https://github.com/xerial/sbt-sql")),
  scmInfo := Some(
    ScmInfo(
      browseUrl = url("https://github.com/xerial/sbt-sql"),
      connection = "scm:git@github.com:xerial/sbt-sql.git"
    )
  ),
  developers := List(
    Developer(id = "leo", name = "Taro L. Saito", email = "leo@xerial.org", url = url("http://xerial.org/leo"))
  ),
  publishTo := sonatypePublishToBundle.value,
  organizationName := "Xerial project",
  organizationHomepage := Some(new URL("http://xerial.org/")),
  description := "A sbt plugin for generating model classes from SQL files",
  publishMavenStyle := true,
  Test / publishArtifact := false,
  pomIncludeRepository := { _ => false },
  parallelExecution := true,
  scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked"),
  libraryDependencies ++= Seq(
    "org.xerial" %% "xerial-lens" % "3.6.0",
    "org.scala-lang" % "scala-compiler" % scalaVersion.value,
    // Scala 2.10 contains parser combinators
    //"org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.5",
    "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    "io.prestosql" % "presto-jdbc" % PRESTO_VERSION % "test"
  ),
  // sbt plugin settings
  sbtPlugin := true,
  scalaVersion := SCALA_2_12,
  scriptedLaunchOpts := {
    scriptedLaunchOpts.value ++
            Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
  },
  crossSbtVersions := Vector("1.3.8")
)

commands += Command.command("bumpPluginVersion") {state =>
  val extracted = Project.extract(state)
  val newVersion = extracted.get(ThisBuild / version)
  val pluginSbt = file(".") ** "src" / "sbt-test" ** "project" ** "plugins.sbt"
  for (f <- pluginSbt.get) {
    state.log.info(s"update sbt-sql plugin version in ${f}")
    val updated = (for (line <- IO.readLines(f)) yield {
      line.replaceAll("""(.+\"sbt-sql(-[a-z]+)?\" % \")([^"]+)("\))""", s"$$1${newVersion}$$4")
    })
    IO.writeLines(f, updated)
  }
  val ret = sys.process.Process("git add src/sbt-test").!
  ret match {
    case 0 => state
    case _ => state.fail
  }
}

lazy val root: Project =
  Project(id = "sbt-sql-root", base = file("."))
 .enablePlugins(ScriptedPlugin)
 .settings(
    buildSettings,
    scriptedBufferLog := false,
    publish := {},
    publishLocal := {},
    publishArtifact := false,
    test := {},
    releaseTagName := {(ThisBuild / version).value},
    releaseCrossBuild := true,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      releaseStepCommandAndRemaining("scripted"),
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      releaseStepCommandAndRemaining("publishSigned"),
      releaseStepCommand("sonatypeBundleRelease"),
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  )
  .aggregate(base, generic, sqlite, presto, td)
  .dependsOn(base, generic, sqlite, presto, td)


lazy val base: Project =
  Project(id = "sbt-sql-base", base = file("base"))
  .settings(
    buildSettings,
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-parser-combinators" % SCALA_PARSER_COMBINATOR_VERSION,
      "org.wvlet.airframe" %% "airframe-surface" % "20.6.2"
    ),
    Compile / resourceGenerators += Def.task {
      val buildProp = (Compile / resourceManaged).value / "org" / "xerial" / "sbt" / "sbt-sql" / "build.properties"
      val buildRev = scala.sys.process.Process("git" :: "rev-parse" :: "HEAD" :: Nil).!!.trim
      val buildTime = ((Compile / sourceDirectory).value / "xerial/sbt/sql/SQLModelClassGenerator.scala").lastModified()
      val contents = s"name=$name\nversion=${version.value}\nbuild_revision=$buildRev\nbuild_time=$buildTime"
      IO.write(buildProp, contents)
      Seq(buildProp)
    }.taskValue
  )

lazy val generic: Project =
  Project(id = "sbt-sql", base = file("generic"))
  .settings(
    buildSettings,
    description := " A sbt plugin for generating model classes from SQL files"
  ).dependsOn(base)

lazy val sqlite: Project =
  Project(id = "sbt-sql-sqlite", base = file("sqlite"))
  .settings(
    buildSettings,
    description := " A sbt plugin for genarting model classes from SQLite SQL files",
    libraryDependencies ++= Seq(
      "org.xerial" % "sqlite-jdbc" % "3.32.3"
    )
  ).dependsOn(base)

lazy val presto: Project =
  Project(id = "sbt-sql-presto", base = file("presto"))
  .settings(
    buildSettings,
    description := " A sbt plugin for generating model classes from Presto SQL files",
    libraryDependencies ++= Seq(
      "io.prestosql" % "presto-jdbc" % PRESTO_VERSION
    )
  ).dependsOn(base)

lazy val td: Project =
  Project(id = "sbt-sql-td", base = file("td"))
  .settings(
    buildSettings,
    description := " A sbt plugin for generating model classes from Treasure Data Presto SQL files",
    libraryDependencies ++= Seq(
      "io.prestosql" % "presto-jdbc" % PRESTO_VERSION
    )
  )
  .dependsOn(base)
