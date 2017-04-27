package xerial.sbt.sql

import xerial.sbt.sql.SQLTemplateParser.FunctionArg

/**
  *
  */
class SQLTemplateParserTest extends Spec {

  "SQLTemplateParser" should {

    "parse function preamble" in {
      val f = SQLTemplateParser.parseFunction("""@(id:Int=0, name:String, timeZone:String="UTC")""")
      f.args should have length (3)
      f.args(0) shouldBe FunctionArg("id", "Int", Some("0"))
      f.args(1) shouldBe FunctionArg("name", "String", None)
      f.args(2) shouldBe FunctionArg("timeZone", "String", Some("UTC"))
    }
  }
}
