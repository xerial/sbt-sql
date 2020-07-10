package xerial.sbt.sql

/**
  */
class SQLTemplateCompilerTest extends Spec {
  "SQLTemplateCompiler" should {

    "handle embedded Scala expression" in {

      val t =
        """@import java.util.Locale
          |@(range:(Int,Int),str:String="test")
          |select ${range._1}, ${range._2}, '${str.toString}'
        """.stripMargin

      SQLTemplateCompiler.compile(t)
    }

  }

}
