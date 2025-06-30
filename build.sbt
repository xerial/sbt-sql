val TRINO_VERSION                   = "476"
val SCALA_PARSER_COMBINATOR_VERSION = "2.4.0"

val SCALA_2_12 = "2.12.20"
ThisBuild / scalaVersion := SCALA_2_12

Global / onChangedBuildSource := ReloadOnSourceChanges

val buildSettings = Seq(
  organization        := "org.xerial.sbt",
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
  publishTo              := sonatypePublishToBundle.value,
  organizationName       := "Xerial project",
  organizationHomepage   := Some(new URL("https://xerial.org/")),
  description            := "A sbt plugin for generating model classes from SQL files",
  publishMavenStyle      := true,
  Test / publishArtifact := false,
  pomIncludeRepository   := { _ => false },
  parallelExecution      := true,
  scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked"),
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-compiler" % scalaVersion.value,
    // Scala 2.10 contains parser combinators
    // "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.5",
    "org.scalatest" %% "scalatest"  % "3.2.19"      % "test",
    "io.trino"       % "trino-jdbc" % TRINO_VERSION % "test"
  ),
  // sbt plugin settings
  sbtPlugin    := true,
  scalaVersion := SCALA_2_12,
  scriptedLaunchOpts := {
    scriptedLaunchOpts.value ++
      Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
  },
  crossSbtVersions := Vector("1.4.6")
)

commands += Command.command("bumpPluginVersion") { state =>
  val extracted  = Project.extract(state)
  val newVersion = extracted.get(ThisBuild / version)
  val pluginSbt  = file(".") ** "src" / "sbt-test" ** "project" ** "plugins.sbt"
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
      publish           := {},
      publishLocal      := {},
      publishArtifact   := false,
      test              := {}
    )
    .aggregate(base, generic, sqlite, duckdb, trino, td)
    .dependsOn(base, generic, sqlite, duckdb, trino, td)

lazy val base: Project =
  Project(id = "sbt-sql-base", base = file("base"))
    .settings(
      buildSettings,
      libraryDependencies ++= Seq(
        "org.scala-lang.modules" %% "scala-parser-combinators" % SCALA_PARSER_COMBINATOR_VERSION,
        "org.wvlet.airframe"     %% "airframe-surface"         % "2025.1.14"
      ),
      Compile / resourceGenerators += Def.task {
        val buildProp = (Compile / resourceManaged).value / "org" / "xerial" / "sbt" / "sbt-sql" / "build.properties"
        val buildRev  = scala.sys.process.Process("git" :: "rev-parse" :: "HEAD" :: Nil).!!.trim
        val buildTime =
          ((Compile / sourceDirectory).value / "xerial/sbt/sql/SQLModelClassGenerator.scala").lastModified()
        val contents = s"name=$name\nversion=${version.value}\nbuild_revision=$buildRev\nbuild_time=$buildTime"
        IO.write(buildProp, contents)
        Seq(buildProp)
      }.taskValue
    )

lazy val generic: Project = {
  project
    .in(file("generic"))
    .settings(
      buildSettings,
      name        := "sbt-sql",
      description := " A sbt plugin for generating model classes from SQL files"
    ).dependsOn(base)
}

lazy val sqlite: Project =
  project
    .in(file("sqlite"))
    .settings(
      buildSettings,
      name        := "sbt-sql-sqlite",
      description := " A sbt plugin for genarting model classes from SQLite SQL files",
      libraryDependencies ++= Seq(
        "org.xerial" % "sqlite-jdbc" % "3.50.2.0"
      )
    ).dependsOn(base)

lazy val duckdb: Project =
  project
    .in(file("duckdb"))
    .settings(
      buildSettings,
      name        := "sbt-sql-duckdb",
      description := " A sbt plugin for genarting model classes from DuckDB SQL files",
      libraryDependencies ++= Seq(
        "org.duckdb" % "duckdb_jdbc" % "1.3.1.0"
      )
    ).dependsOn(base)

lazy val trino: Project =
  project
    .in(file("trino"))
    .settings(
      buildSettings,
      name        := "sbt-sql-trino",
      description := " A sbt plugin for generating model classes from Trino SQL files",
      libraryDependencies ++= Seq(
        "io.trino" % "trino-jdbc" % TRINO_VERSION
      )
    ).dependsOn(base)

lazy val td: Project =
  project
    .in(file("td"))
    .settings(
      buildSettings,
      name        := "sbt-sql-td",
      description := " A sbt plugin for generating model classes from Treasure Data SQL files",
      libraryDependencies ++= Seq(
        "io.trino" % "trino-jdbc" % TRINO_VERSION
      )
    ).dependsOn(base)
