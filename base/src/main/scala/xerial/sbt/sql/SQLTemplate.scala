package xerial.sbt.sql

import xerial.core.log.Logger

import scala.util.matching.Regex.Match

object SQLTemplate extends Logger {
  sealed trait Fragment
  case class Text(s: String) extends Fragment
  case class Param(name: String, typeName: String) extends Fragment

  def apply(sql: String): SQLTemplate = SQLTemplateParser.parse(sql)
}

import xerial.sbt.sql.Preamble._
import xerial.sbt.sql.SQLTemplateParser._

case class SQLTemplate(orig: String, params: Seq[Preamble.FunctionArg], imports: Seq[Preamble.Import]) {
  def noParam: String = removeParamType(orig)
  def render(args: Seq[Any]): String = {
    var rendered = noParam
    for ((p, arg) <- params.zip(args)) {
      rendered = rendered.replaceAll(s"\\$$\\{${p.name}\\}", arg.toString)
    }
    rendered
  }

  def populated: String = {
    val newParams = Seq.newBuilder[String]
    val template = embeddedParamPattern.replaceAllIn(orig, {m: Match =>
      val name = m.group(1)
      params.find(_.name == name) match {
        case Some(p) =>
          val typeName = p.typeName
          val defaultValue = p.defaultValue.orElse(Option(m.group(5)))
          val v = typeName match {
            case "String" => "dummy"
            case "Int" => "0"
            case "Long" => "0"
            case "Float" => "0.0"
            case "Double" => "0.0"
            case "Boolean" => "true"
            case "SQL" | "sql" => ""
            case _ => ""
          }
          newParams += defaultValue.getOrElse(v)
        case None =>
      }
      "%s"
    })
    String.format(template, newParams.result(): _*)
  }
}
