package xerial.sbt.sql

import java.io.File

import xerial.core.log.LoggerFactory

/**
  *
  */
class SQLModelClassGeneratorTest extends Spec {
  "SQLModelClassGenerator" should {
    "genarate case class code" in {
      val g = new SQLModelClassGenerator(
        JDBCConfig(
          driver = "com.facebook.presto.jdbc.PrestoDriver",
          url = "jdbc:presto://api-presto.treasuredata.com:443/td-presto",
          user = sys.env("TD_API_KEY"),
          password = ""
        ),
        new DebugLogSupport(LoggerFactory(classOf[SQLModelClassGenerator]))
      )
      g.generate(GeneratorConfig(
        new File("base/src/test/sql/presto"),
        new File("target/sbt-0.13/src_managed/test"),
        new File("target/sbt-0.13/resource_managed/test")
      ))
    }
  }
}
