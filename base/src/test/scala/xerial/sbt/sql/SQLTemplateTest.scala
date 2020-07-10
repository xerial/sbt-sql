package xerial.sbt.sql

import xerial.sbt.sql.Preamble.FunctionArg

/**
  */
class SQLTemplateTest extends Spec {

  "SQLTemplate" should {
    "extract embedded variables" in {
      val params = SQLTemplateParser.extractParam("""
          |select * from sample_datasets.nasdaq
          |where td_time_range(time, '${start:String}', '${end:String}')
          |${cond:sql=AND time > 0}
        """.stripMargin)

      info(params)
      params.length shouldBe 3
      params(0) shouldBe FunctionArg("start", "String", None)             // , 3, 27, 42)
      params(1) shouldBe FunctionArg("end", "String", None)               // , 3, 46, 59)
      params(2) shouldBe FunctionArg("cond", "sql", Some("AND time > 0")) // , 4, 0, 24)
    }

    "remove type param" in {
      val removed = SQLTemplateParser.removeParamType("select ${a:Int}, ${b:String}")
      removed shouldBe "select ${a}, ${b}"
    }

    "populate params" in {
      val populated =
        SQLTemplate("select ${a:Int}, '${b:String}', ${c:Float}, ${d:Boolean}, '${e:String}', ${f:Double}").populated
      populated shouldBe "select 0, 'dummy', 0.0, true, 'dummy', 0.0"
    }
  }

}
