package xerial.sbt.sql

import xerial.sbt.sql.SQLTemplate.TemplateParam

/**
  *
  */
class SQLTemplateTest extends Spec {

  "SQLTemplate" should {
    "extract embedded variables" in {
      val params = SQLTemplate.extractParam(
        """
           |select * from sample_datasets.nasdaq
           |where td_time_range(time, '${start:String}', '${end:String}')
         """.stripMargin)

      info(params)
      params.length shouldBe 2
      params(0) shouldBe TemplateParam("start", "String", 3, 27, 42)
      params(1) shouldBe TemplateParam("end", "String", 3, 46, 59)
    }

    "remove type param" in {
      val removed = SQLTemplate.removeParamType("select ${a:Int}, ${b:String}")
      removed shouldBe "select ${a}, ${b}"

    }
  }

}
