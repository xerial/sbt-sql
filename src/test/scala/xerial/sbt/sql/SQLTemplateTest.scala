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
           |where TD_TIME_RANGE(time, '${start:String}', '${end:String}')
         """.stripMargin)

      info(params)
      params.length shouldBe 2
      params(0) shouldBe TemplateParam("start", "String")
      params(1) shouldBe TemplateParam("end", "String")
    }
  }

}
