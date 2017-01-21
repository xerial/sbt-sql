package xerial.sbt.sql

import xerial.core.log.Logger

import scala.io.Source
import scala.util.matching.Regex.Match


case class SQLTemplate(orig:String, params:Seq[TemplateParam], noParam:String, populated:String)
case class TemplateParam(name:String, typeName:String, line:Int, start:Int, end:Int)
/**
  *
  */
object SQLTemplate extends Logger {

  val embeddedParamPattern = """\$\{\s*(\w+)\s*(:\s*(\w+))?\s*\}""".r

  def apply(sql:String) : SQLTemplate = {
    SQLTemplate(sql, extractParam(sql), removeParamType(sql), populateParam(sql))
  }

  def extractParam(sql:String) : Seq[TemplateParam] = {
    // TODO remove comment lines
    val params = Seq.newBuilder[TemplateParam]
    for ((line, lineNum) <- Source.fromString(sql).getLines().zipWithIndex) {
      for (m <- embeddedParamPattern.findAllMatchIn(line)) {
        val name = m.group(1)
        val typeName = Option(m.group(3))
        params += TemplateParam(name, typeName.getOrElse("String"), lineNum+1, m.start, m.end)
      }
    }
    params.result()
  }

  def removeParamType(sql:String) : String = {
    embeddedParamPattern.replaceAllIn(sql, { m: Match =>
      val name = m.group(1)
      "\\${" + name + "}"
    })
  }

  def populateParam(sql:String) : String = {
    val params = Seq.newBuilder[String]
    val template = embeddedParamPattern.replaceAllIn(sql, { m: Match =>
      val name = m.group(1)
      val typeName = Option(m.group(3)).getOrElse("String")
      val v = typeName match {
        case "String" => "dummy"
        case "Int" => "0"
        case "Long" => "0"
        case "Float" => "0.0"
        case "Double" => "0.0"
        case "Boolean" => "true"
        case _ => ""
      }
      params += v
      "%s"
    })

    String.format(template, params.result():_*)
  }

}
