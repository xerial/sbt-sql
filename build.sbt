import ReleaseTransformations._

val PRESTO_VERSION = "326"
val SCALA_2_12 = "2.12.10"
scalaVersion in Global := SCALA_2_12

val buildSettings = Seq(
  organization := "org.xerial.sbt",
  sonatypeProfileName := "org.xerial",
  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  pomExtra in ThisBuild := {
    <url>http://xerial.org/</url>
    <scm>
      <connection>scm:git:github.com/xerial/sbt-sql.git</connection>
      <developerConnection>scm:git:git@github.com:xerial/sbt-sql.git</developerConnection>
      <url>https://github.com/xerial/sbt-sql</url>
    </scm>
    <developers>
      <developer>
        <id>leo</id>
        <name>Taro L. Saito</name>
        <url>http://xerial.org/leo</url>
      </developer>
    </developers>
  },
  publishTo := Some(
    if (isSnapshot.value)
      Opts.resolver.sonatypeSnapshots
    else
      Opts.resolver.sonatypeStaging
  ),
  organizationName := "Xerial project",
  organizationHomepage := Some(new URL("http://xerial.org/")),
  description := "A sbt plugin for generating model classes from SQL files",
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := {_ => false},
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
//  scalaCompilerBridgeSource := {
//    val sv = appConfiguration.value.provider.id.version
//    ("org.scala-sbt" % "compiler-interface" % sv % "component").sources
//  }
)

commands += Command.command("bumpPluginVersion") {state =>
  val extracted = Project.extract(state)
  val newVersion = extracted.get(version in ThisBuild)
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
    releaseTagName := {(version in ThisBuild).value},
    releaseCrossBuild := true,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      releaseStepCommandAndRemaining("^ scripted"),
      setReleaseVersion,
      releaseStepCommand("bumpPluginVersion"),
      commitReleaseVersion,
      tagRelease,
      releaseStepCommandAndRemaining("^ publishSigned"),
      setNextVersion,
      releaseStepCommand("bumpPluginVersion"),
      commitNextVersion,
      releaseStepCommand("sonatypeReleaseAll"),
      pushChanges
    )
  ).aggregate(base, generic, sqlite, presto, td)

lazy val base: Project =
  Project(id = "sbt-sql-base", base = file("base"))
  .settings(
    buildSettings,
    resourceGenerators in Compile += Def.task {
      val buildProp = (resourceManaged in Compile).value / "org" / "xerial" / "sbt" / "sbt-sql" / "build.properties"
      val buildRev = scala.sys.process.Process("git" :: "rev-parse" :: "HEAD" :: Nil).!!.trim
      val buildTime = ((sourceDirectory in Compile).value / "xerial/sbt/sql/SQLModelClassGenerator.scala").lastModified()
      val contents = s"name=$name\nversion=${version.value}\nbuild_revision=$buildRev\nbuild_time=$buildTime"
      IO.write(buildProp, contents)
      Seq(buildProp)
    }.taskValue
  )

lazy val generic: Project =
  Project(id = "sbt-sql", base = file("generic"))
  .settings(
    buildSettings,
    description := " A sbt plugin for generating model classes from SQL files",
    libraryDependencies ++= Seq(
    )
  ).dependsOn(base)

lazy val sqlite: Project =
  Project(id = "sbt-sql-sqlite", base = file("sqlite"))
  .settings(
    buildSettings,
    description := " A sbt plugin for genarting model classes from SQLite SQL files",
    libraryDependencies ++= Seq(
      "org.xerial" % "sqlite-jdbc" % "3.20.0"
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
