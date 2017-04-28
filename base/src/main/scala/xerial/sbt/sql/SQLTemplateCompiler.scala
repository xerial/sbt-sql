package xerial.sbt.sql

import xerial.lens.TypeUtil

import scala.util.Try

/**
  *
  */
object SQLTemplateCompiler extends xerial.core.log.Logger {

  import scala.reflect.runtime.currentMirror
  import scala.tools.reflect.ToolBox

  private val toolBox = currentMirror.mkToolBox()

  private def defaultValueFor(typeName: String): Any = typeName match {
    case "SQL" | "sql" => ""
    case "String" => "dummy"
    case "Int" => 0
    case "Long" => 0L
    case "Float" => 0.0
    case "Double" => 0.0f
    case "Boolean" => true
    case tuple if typeName.startsWith("(") =>
      val a = tuple.trim.substring(1, tuple.length - 1).split(",")
      val e = a.map(x => defaultValueFor(x))
      // TODO proper parsing of tuple types
      e.length match {
        case 1 => (e(0))
        case 2 => (e(0), e(1))
        case 3 => (e(0), e(1), e(2))
        case 4 => (e(0), e(1), e(2), e(3))
        case 5 => (e(0), e(1), e(2), e(3), e(4))
        case _ => null
      }
    case _ =>
      Try(TypeUtil.zero(Class.forName(typeName))).getOrElse(null)
  }

  def compile(sqlTemplate: String) : SQLTemplate = {
    val parsed = SQLTemplateParser.parse(sqlTemplate)
    val params = parsed.args
    val imports = parsed.imports.map(x => s"import ${x.target}").mkString("\n")
    val paramLength = params.length
    val methodArgs = params.map(x => s"${x.name}:${x.functionArgType}").mkString(", ")
    val functionArgs = {
      val a = (params.map {p => s"${p.functionArgType}"}).mkString(", ")
      if (paramLength > 1 || (paramLength == 1 && a.startsWith("("))) // tuple type only
        s"($a)"
      else
        a
    }
    val sqlCode = "s\"\"\"" + parsed.noParamSQL + "\"\"\""
    val funDef =
      s"""$imports
         |new (${functionArgs} => String) {
         |  def apply(${methodArgs}): String = {
         |    $sqlCode
         |  }
         |}
         |
     """.stripMargin
    debug(s"function def:\n${funDef}")

    val code = toolBox.eval(toolBox.parse(funDef))

    val p = (params.map {p =>
      p.defaultValue.getOrElse(defaultValueFor(p.typeName))
    }).toIndexedSeq

    val populatedSQL : String = paramLength match {
      case 0 =>
        code.asInstanceOf[Function0[String]].apply()
      case 1 =>
        code.asInstanceOf[Function1[Any, String]].apply(p(0))
      case 2 =>
        code.asInstanceOf[Function2[Any, Any, String]].apply(p(0), p(1))
      case 3 =>
        code.asInstanceOf[Function3[Any, Any, Any, String]].apply(p(0), p(1), p(2))
      case 4 =>
        code.asInstanceOf[Function4[Any, Any, Any, Any, String]].apply(p(0), p(1), p(2), p(3))
      case 5 =>
        code.asInstanceOf[Function5[Any, Any, Any, Any, Any, String]].apply(p(0), p(1), p(2), p(3), p(4))
      case 6 =>
        code.asInstanceOf[Function6[Any, Any, Any, Any, Any, Any, String]].apply(p(0), p(1), p(2), p(3), p(4), p(5))
      case 7 =>
        code.asInstanceOf[Function7[Any, Any, Any, Any, Any, Any, Any, String]].apply(p(0), p(1), p(2), p(3), p(4), p(5), p(6))
      case 8 =>
        code.asInstanceOf[Function8[Any, Any, Any, Any, Any, Any, Any, Any, String]].apply(p(0), p(1), p(2), p(3), p(4), p(5), p(6), p(7))
      case 9 =>
        code.asInstanceOf[Function9[Any, Any, Any, Any, Any, Any, Any, Any, Any, String]].apply(p(0), p(1), p(2), p(3), p(4), p(5), p(6), p(7), p(8))
      case 10 =>
        code.asInstanceOf[Function10[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, String]].apply(p(0), p(1), p(2), p(3), p(4), p(5), p(6), p(7), p(8), p(9))
      case 11 =>
        code.asInstanceOf[Function11[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, String]].apply(p(0), p(1), p(2), p(3), p(4), p(5), p(6), p(7), p(8), p(9), p(10))
      case 12 =>
        code.asInstanceOf[Function12[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any,Any, String]].apply(p(0), p(1), p(2), p(3), p(4), p(5), p(6), p(7), p(8), p(9), p(10), p(11))
      case 13 =>
        code.asInstanceOf[Function13[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any,Any, Any, String]].apply(p(0), p(1), p(2), p(3), p(4), p(5), p(6), p(7), p(8), p(9), p(10),p(11),p(12))
      case 14 =>
        code.asInstanceOf[Function14[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any,Any, Any, Any, String]].apply(p(0), p(1), p(2), p(3), p(4), p(5), p(6), p(7), p(8), p(9), p(10),p(11),p(12),p(13))
      case 15 =>
        code.asInstanceOf[Function15[Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any,Any, Any, Any, Any, String]].apply(p(0), p(1), p(2), p(3), p(4), p(5), p(6), p(7), p(8), p(9), p(10),p(11),p(12),p(13),p(14))
      case other =>
        warn(s"Too many parameters in SQL template:\n${sqlTemplate}")
        parsed.noParamSQL
    }

    debug(s"populated SQL:\n${populatedSQL}")

    new SQLTemplate(
      orig = parsed.sql,
      noParam = parsed.noParamSQL,
      populated = populatedSQL,
      params = params,
      imports = parsed.imports
    )
  }



}