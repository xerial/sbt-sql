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

    "populate params" in {
      val populated = SQLTemplate.populateParam("select ${a:Int}, '${b:String}', ${c:Float}, ${d:Boolean}, '${e:String}', ${f:Double}")
      populated shouldBe "select 0, 'dummy', 0.0, true, 'dummy', 0.0"
    }
  }

}
