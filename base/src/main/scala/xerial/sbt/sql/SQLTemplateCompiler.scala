package xerial.sbt.sql

import wvlet.airframe.surface.{Surface, Zero}
import wvlet.airframe.surface.reflect.{ReflectSurfaceFactory, ReflectTypeUtil}
import wvlet.log.LogSupport

import scala.util.{Failure, Success, Try}

/** Reflection-free SQLTemplateCompiler that works for both Scala 2 and 3 */
object SQLTemplateCompiler extends LogSupport {

  private def defaultValueFor(typeName: String): Any =
    typeName match {
      case "SQL" | "sql" => ""
      case "String"      => "dummy"
      case "Int"         => 0
      case "Long"        => 0L
      case "Float"       => 0.0f
      case "Double"      => 0.0
      case "Boolean"     => true
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
          case 6 => (e(0), e(1), e(2), e(3), e(4), e(5))
          case 7 => (e(0), e(1), e(2), e(3), e(4), e(5), e(6))
          case _ => new UnsupportedOperationException(s"tuple size ${e.length} is not supported: ${typeName}")
        }
      case _ => Zero.zeroOf(ReflectSurfaceFactory.ofTypeName(typeName))
    }

  def compile(sqlTemplate: String): SQLTemplate = {
    val parsed  = SQLTemplateParser.parse(sqlTemplate)
    val params  = parsed.args
    val imports = parsed.imports.map(x => s"import ${x.target}").mkString("\n")

    val (defaultParams, otherParams) = params.partition(_.defaultValue.isDefined)

    // Build a map of parameter values
    val defaultParamMap = defaultParams.map(p => p.name -> p.defaultValue.get).toMap
    val otherParamMap = otherParams.map(p => p.name -> defaultValueFor(p.typeName)).toMap
    val paramMap = defaultParamMap ++ otherParamMap
    
    // Start with the raw SQL
    var populatedSQL = parsed.sql.sql
    
    // Process embedded expressions
    parsed.sql.embeddedExpressions.foreach { expr =>
      val exprCode = expr.code.trim
      val replacement = evaluateExpression(exprCode, paramMap)
      val placeholder = s"$${${expr.code}}"
      populatedSQL = populatedSQL.replace(placeholder, replacement)
    }

    SQLTemplate(params, imports, parsed.sql, populatedSQL)
  }

  private def evaluateExpression(expr: String, paramMap: Map[String, Any]): String = {
    // Handle simple cases without reflection
    expr match {
      // Direct parameter reference
      case param if paramMap.contains(param) => 
        paramMap(param).toString
        
      // String literal
      case s if s.startsWith("\"") && s.endsWith("\"") => 
        s.substring(1, s.length - 1)
        
      // Number literal
      case n if n.matches("-?\\d+(\\.\\d+)?") => 
        n
        
      // Boolean literal
      case "true" | "false" => 
        expr
        
      // Simple method calls on parameters
      case methodCall if methodCall.contains(".") =>
        val parts = methodCall.split("\\.", 2)
        val objName = parts(0)
        val method = parts(1)
        
        paramMap.get(objName) match {
          case Some(value) =>
            // Handle common string methods
            if (method == "toString" || method == "toString()") {
              value.toString
            } else if (method.startsWith("substring(") && method.endsWith(")")) {
              // Simple substring handling
              try {
                val args = method.substring(10, method.length - 1).split(",").map(_.trim.toInt)
                if (args.length == 1) {
                  value.toString.substring(args(0))
                } else if (args.length == 2) {
                  value.toString.substring(args(0), args(1))
                } else {
                  s"(${expr})"
                }
              } catch {
                case _: Exception => s"(${expr})"
              }
            } else {
              s"(${expr})"
            }
          case None => s"(${expr})"
        }
        
      // String concatenation
      case concat if concat.contains("+") =>
        // Simple handling of string concatenation
        val parts = concat.split("\\+").map(_.trim)
        val evaluated = parts.map(p => evaluateExpression(p, paramMap))
        evaluated.mkString("")
        
      // Default: return as placeholder
      case _ => 
        s"(${expr})"
    }
  }
}