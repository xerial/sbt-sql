package xerial.sbt.sql

import xerial.core.log.Logger

import scala.io.Source
import scala.util.parsing.combinator._


/**
  *
  */
object SQLTemplateParser extends Logger {

  def parse0(template:String) {
    val imports = Seq.newBuilder[String]
    var function: Option[String] = None
    val remaining = Seq.newBuilder[String]

    for (line <- Source.fromString(template).getLines()) yield {
      line match {
        case line if line.startsWith("@import ") =>
          val importClass = line.replaceAll("^@import ", "").trim
          info(importClass)
        case functionArgs if line.startsWith("@(") =>
          parseFunction(functionArgs)
          info(s"function args:${functionArgs}")
        case other =>
          remaining += line
      }
    }

    val sql = remaining.result()
  }

  def parseFunction(f:String) : Function = {
    PreambleParser.parse(PreambleParser.function, f) match {
      case PreambleParser.Success(matched, _) => matched
      case other => throw new IllegalArgumentException(other.toString)
    }
  }

  case class Function(args:Seq[FunctionArg])
  case class FunctionArg(name:String, typeName:String, defaultValue:Option[String])
  case class Import(target:String)

  object PreambleParser extends JavaTokenParsers {
    override def skipWhitespace = true

    def str: Parser[String] = stringLiteral ^^ { x => x.substring(1,x.length-1) }
    def value: Parser[String] = ident | str | decimalNumber | wholeNumber | floatingPointNumber
    def defaultValue : Parser[String] = "=" ~ value ^^ { case _ ~ v => v }
    def arg : Parser[FunctionArg] = ident ~ ":" ~ ident ~ opt(defaultValue) ^^ { case n ~ _ ~ t ~ opt => FunctionArg(n, t, opt) }
    def args : Parser[Seq[FunctionArg]] = arg ~ rep(',' ~ arg) ^^ { case first ~ rest => Seq(first) ++ rest.map(_._2).toSeq }

    def function: Parser[Function] = "@(" ~ args ~ ")" ^^ { case _ ~ args ~ _ => Function(args) }
    def importStmt: Parser[Import] = "@import" ~ ident ^^ { case _ ~ i => Import(i.toString) }
  }

}

