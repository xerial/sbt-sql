package xerial.sbt.sql

import java.io.File

import xerial.core.log.LoggerFactory

/**
  *
  */
class SQLModelClassGeneratorTest extends Spec {
  "SQLModelClassGenerator" should {
    "generate case class code" in {
      val g = new SQLModelClassGenerator(
        JDBCConfig(
          driver = "io.prestosql.jdbc.PrestoDriver",
          url = "jdbc:presto://api-presto.treasuredata.com:443/td-presto?SSL=true",
          user = sys.env("TD_API_KEY"),
          password = "dummy"
        )
      )
      g.generate(GeneratorConfig(
        new File("base/src/test/sql/presto"),
        new File("target/sbt-1.0/src_managed/test")
      ))
    }
  }
}
