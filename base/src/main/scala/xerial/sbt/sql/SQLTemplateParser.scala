package xerial.sbt.sql

import xerial.core.log.Logger

import scala.io.Source
import scala.util.matching.Regex.Match
import scala.util.parsing.combinator._

sealed trait Preamble

object Preamble {
  case class Function(args:Seq[FunctionArg]) extends Preamble
  case class FunctionArg(name:String, typeName:String, defaultValue:Option[String]) {
    override def toString = s"${name}:${typeName}${defaultValue.map(x => s"=${x}").getOrElse("")}"

    def isSameType(a:FunctionArg) = name == a.name && typeName == a.typeName

    def quotedValue: String = {
      typeName match {
        case "String" | "SQL" | "sql" => "\"" + defaultValue.get + "\""
        case other => defaultValue.get
      }
    }
    def functionArgType: String = {
      typeName match {
        case "SQL" | "sql" => "String"
        case other => other
      }
    }
  }
  case class Import(target:String) extends Preamble
}

import Preamble._
/**
  *
  */
object SQLTemplateParser extends Logger {

  case class Pos(line:Int, pos:Int)
  case class ParseError(message:String, pos:Option[Pos]) extends Exception(message)

  def parse(template:String): SQLTemplate = {
    val preamble = Seq.newBuilder[Preamble]
    val remaining = Seq.newBuilder[String]

    for ((line, lineNum) <- Source.fromString(template).getLines().zipWithIndex) {
      if(line.startsWith("@")) {
        PreambleParser.parse(PreambleParser.preamble, line) match {
          case PreambleParser.Success(matched, _) => preamble += matched
          case other =>
            throw ParseError(other.toString, Some(Pos(lineNum+1, 0)))
        }
      }
      else {
        remaining += line
      }
    }

    val sql = remaining.result().mkString("\n")
    val p = preamble.result()
    val imports = p.collect{case i:Import => i}
    val f = {
      val defs = p.collect {case f: Function => f}
      if (defs.size > 1) {
        warn(s"Multiple function definitions are found:\n${defs.mkString("\n")}")
      }
      defs.headOption
    }

    val parametersInsideSQLBody = extractParam(sql)
    if(f.nonEmpty) {
      for (x <- parametersInsideSQLBody) {
        if (!f.get.args.exists(_.name == x.name)) {
          throw ParseError(s"${x} is not found in the function definition", None)
        }
      }
    }

    // Allow SQL template without any function header for backward compatibility
    SQLTemplate(sql, f.map(_.args).getOrElse(parametersInsideSQLBody), imports)
  }

  def parseFunction(f:String) : Function = {
    PreambleParser.parse(PreambleParser.function, f) match {
      case PreambleParser.Success(matched, _) => matched
      case other => throw new IllegalArgumentException(other.toString)
    }
  }


  object PreambleParser extends JavaTokenParsers {
    override def skipWhitespace = true

    def str: Parser[String] = stringLiteral ^^ { x => x.substring(1,x.length-1) }
    def value: Parser[String] = ident | str | decimalNumber | wholeNumber | floatingPointNumber
    def defaultValue : Parser[String] = "=" ~ value ^^ { case _ ~ v => v }
    def arg : Parser[FunctionArg] = ident ~ ":" ~ ident ~ opt(defaultValue) ^^ { case n ~ _ ~ t ~ opt => FunctionArg(n, t, opt) }
    def args : Parser[Seq[FunctionArg]] = arg ~ rep(',' ~ arg) ^^ { case first ~ rest => Seq(first) ++ rest.map(_._2).toSeq }

    def function: Parser[Function] = "@(" ~ args ~ ")" ^^ { case _ ~ args ~ _ => Function(args) }
    def importStmt: Parser[Import] = "@import" ~ classRef ^^ { case _ ~ i => Import(i.toString) }
    def classRef: Parser[String] = ident ~ rep('.' ~ ident) ^^ {
      case h ~ t => (h :: t.map(_._2)).mkString(".")
    }
    def preamble : Parser[Preamble] = function | importStmt
  }

  val embeddedParamPattern = """\$\{\s*(\w+)\s*(:\s*(\w+))?\s*(=\s*([^\}]+)\s*)?\}""".r

  def extractParam(sql:String) : Seq[FunctionArg] = {
    // TODO remove comment lines
    val params = Seq.newBuilder[FunctionArg]
    for ((line, lineNum) <- Source.fromString(sql).getLines().zipWithIndex) {
      for (m <- embeddedParamPattern.findAllMatchIn(line)) {
        val name = m.group(1)
        val typeName = Option(m.group(3))
        val defaultValue = Option(m.group(5))
        params += FunctionArg(name, typeName.getOrElse("String"), defaultValue) // , lineNum+1, m.start, m.end)
      }
    }
    // Dedup by preserving orders
    val lst = params.result
    var seen = Set.empty[String]
    val result = for(p <- params.result if !seen.contains(p.name)) yield {
      seen += p.name
      p
    }
    result.toSeq
  }

  def removeParamType(sql:String) : String = {
    embeddedParamPattern.replaceAllIn(sql, { m: Match =>
      val name = m.group(1)
      "\\${" + name + "}"
    })
  }

}

