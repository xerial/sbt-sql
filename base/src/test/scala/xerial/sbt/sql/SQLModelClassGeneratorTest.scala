package xerial.sbt.sql

import java.io.File
import java.sql.{JDBCType, Types}

import xerial.sbt.sql.DataType.OptionType
import xerial.sbt.sql.SQLModelClassGenerator.JDBCResultColumn

/**
 *
 */
class SQLModelClassGeneratorTest
        extends Spec
{
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

    "support optional types" in {
      val columns = Seq(
        JDBCResultColumn("id", "varchar", Types.VARCHAR, true),
        JDBCResultColumn("param__optional", "bigint", Types.BIGINT, true),
        JDBCResultColumn("type", "varchar", Types.VARCHAR, true),
      )
      val schema = SQLModelClassGenerator.generateSchema(columns)
      schema shouldBe Schema(
        IndexedSeq(
          Column("id", DataType.StringType, JDBCType.VARCHAR, true),
          Column("param", OptionType(DataType.LongType), JDBCType.BIGINT, true),
          // quote Scala reserved words
          Column("`type`", DataType.StringType, JDBCType.VARCHAR, true),
        )
      )
    }
  }
}
