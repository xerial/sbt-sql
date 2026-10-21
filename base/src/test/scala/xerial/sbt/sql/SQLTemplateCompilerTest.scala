package xerial.sbt.sql

/** */
class SQLTemplateCompilerTest extends Spec {
  "SQLTemplateCompiler" should {

    "handle embedded Scala expression" in {

      val t =
        """@import java.util.Locale
          |@(range:(Int,Int),str:String="test")
          |select ${range._1}, ${range._2}, '${str.toString}'
        """.stripMargin

      val compiled = SQLTemplateCompiler.compile(t)
      compiled.populated.trim shouldBe "select 0, 0, 'test'"
    }

    "populate parameters with default values" in {
      val t =
        """@(id:Int, name:String, active:Boolean)
          |select * from t where id = ${id} and name = '${name}' and active = ${active}
        """.stripMargin

      val compiled = SQLTemplateCompiler.compile(t)
      compiled.populated.trim shouldBe "select * from t where id = 0 and name = 'dummy' and active = true"
    }

    "report invalid Scala expressions" in {
      val t =
        """@(id:Int)
          |select ${id.unknownMethod}
        """.stripMargin

      val e = intercept[Throwable] {
        SQLTemplateCompiler.compile(t)
      }
      e.getMessage should include("unknownMethod")
    }

  }

}
