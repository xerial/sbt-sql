package xerial.sbt.sql

import xerial.core.log.Logger

object SQLTemplate extends Logger {
  sealed trait Fragment
  case class Text(s: String) extends Fragment
  case class Param(name: String, typeName: String) extends Fragment

  def apply(sql: String): SQLTemplate = SQLTemplateCompiler.compile(sql)
}

case class SQLTemplate(orig: String, noParam: String, populated: String, params: Seq[Preamble.FunctionArg], imports: Seq[Preamble.Import]) {
  def render(args: Seq[Any]): String = {
    var rendered = noParam
    for ((p, arg) <- params.zip(args)) {
      rendered = rendered.replaceAll(s"\\$$\\{${p.name}\\}", arg.toString)
    }
    rendered
  }
}
