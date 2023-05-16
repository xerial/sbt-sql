package xerial.sbt.sql

import wvlet.log.LogSupport

object SQLTemplate extends LogSupport {
  sealed trait Fragment
  case class Text(s: String)                       extends Fragment
  case class Param(name: String, typeName: String) extends Fragment

  def apply(sql: String): SQLTemplate = SQLTemplateCompiler.compile(sql)
}

case class SQLTemplate(
    sql: String,
    populated: String,
    params: Seq[Preamble.FunctionArg],
    imports: Seq[Preamble.Import],
    optionals: Seq[Preamble.Optional]
) {
  def optionalParams: Set[String] = optionals.map(_.columns).flatten.toSet[String]
}
