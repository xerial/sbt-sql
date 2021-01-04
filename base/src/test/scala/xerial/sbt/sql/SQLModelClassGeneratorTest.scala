package xerial.sbt.sql

import java.io.File
import java.sql.{JDBCType, Types}

import xerial.sbt.sql.DataType.{ArrayType, MapType, OptionType}
import xerial.sbt.sql.SQLModelClassGenerator.JDBCResultColumn

/**
  */
class SQLModelClassGeneratorTest extends Spec {
  "SQLModelClassGenerator" should {
    "generate case class code" in {
      if (sys.env.get("TD_API_KEY").isEmpty) {
        // Skip tests that requrie TD_API_KEY
        pending
      }
      val g = new SQLModelClassGenerator(
        JDBCConfig(
          driver = "io.trino.jdbc.PrestoDriver",
          url = "jdbc:presto://api-presto.treasuredata.com:443/td-presto?SSL=true",
          user = sys.env("TD_API_KEY"),
          password = "dummy"
        )
      )
      g.generate(
        GeneratorConfig(
          new File("base/src/test/sql/presto"),
          new File("target/sbt-1.0/src_managed/test")
        )
      )
    }

    "generate Scala type names" in {
      MapType(DataType.StringType, DataType.LongType).name shouldBe "Map[String, Long]"
      ArrayType(DataType.StringType).name shouldBe "Array[String]"
      OptionType(DataType.StringType).name shouldBe "Option[String]"
    }

    "support optional types" in {
      val columns = Seq(
        JDBCResultColumn("id", "varchar", Types.VARCHAR, true),
        JDBCResultColumn("param__optional", "bigint", Types.BIGINT, true),
        JDBCResultColumn("type", "varchar", Types.VARCHAR, true),
        JDBCResultColumn("end_at", "bigint", Types.BIGINT, true)
      )
      val schema = SQLModelClassGenerator.generateSchema(columns, Set("end_at"))
      schema shouldBe Schema(
        IndexedSeq(
          Column("id", DataType.StringType, JDBCType.VARCHAR, true),
          Column("param", OptionType(DataType.LongType), JDBCType.BIGINT, true),
          // quote Scala reserved words
          Column("`type`", DataType.StringType, JDBCType.VARCHAR, true),
          // Optional type specified by preamble
          Column("end_at", OptionType(DataType.LongType), JDBCType.BIGINT, true)
        )
      )
    }
  }
}
