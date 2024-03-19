sys.props.get("plugin.version") match {
  case Some(x) => addSbtPlugin("org.xerial.sbt" % "sbt-sql" % x)
  case _ => sys.error("""|The system property 'plugin.version' is not defined.
                               |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
}
// Add your jdbc driver dependency for checking the result schema
libraryDependencies ++= Seq(
  "org.xerial" % "sqlite-jdbc" % "3.45.2.0"
)
