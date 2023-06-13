package xerial.sbt.sql

import xerial.sbt.sql.Preamble.FunctionArg

/** */
class SQLTemplateParserTest extends Spec {

  "SQLTemplateParser" should {

    "parse function preamble" in {
      val f = SQLTemplateParser.parseFunction("""@(id:Int=0, name:String, timeZone:String="UTC", longVal:Long=100L)""")
      f.args should have length (4)
      f.args(0) shouldBe FunctionArg("id", "Int", Some("0"))
      f.args(1) shouldBe FunctionArg("name", "String", None)
      f.args(2) shouldBe FunctionArg("timeZone", "String", Some("UTC"))
      f.args(3) shouldBe FunctionArg("longVal", "Long", Some("100L"))
    }
  }
}
