package xerial.sbt.sql

import wvlet.airframe.surface.{Surface, Zero}
import wvlet.airframe.surface.reflect.{ReflectSurfaceFactory, ReflectTypeUtil}
import wvlet.log.LogSupport

import scala.util.{Failure, Success, Try}

/** 
 * Scala 3 version of SQLTemplateCompiler
 * 
 * Since runtime reflection is not available in Scala 3, this version
 * evaluates SQL templates using string interpolation at compile time.
 */
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
        e.length match {
          case 1 => (e(0))
          case 2 => (e(0), e(1))
          case 3 => (e(0), e(1), e(2))
          case 4 => (e(0), e(1), e(2), e(3))
          case 5 => (e(0), e(1), e(2), e(3), e(4))
          case _ => null
        }
      case _ =>
        Try(Zero.zeroOf(ReflectSurfaceFactory.ofTypeName(typeName))).toOption.getOrElse(null)
    }

  def compile(sqlTemplate: String): SQLTemplate = {
    val parsed  = SQLTemplateParser.parse(sqlTemplate)
    val params  = parsed.args
    val imports = parsed.imports

    val (defaultParams, otherParams) = params.partition(_.defaultValue.isDefined)

    // Create a context with parameter values
    val context = collection.mutable.Map[String, Any]()
    
    // Add default parameters to context
    defaultParams.foreach { p =>
      val value = p.defaultValue.get match {
        case s if s.startsWith("\"") && s.endsWith("\"") => 
          s.substring(1, s.length - 1)
        case v => v
      }
      context(p.name) = value
    }
    
    // Add other parameters with default values
    otherParams.foreach { p =>
      context(p.name) = defaultValueFor(p.typeName)
    }

    // Process the SQL string to replace ${...} expressions
    val populatedSQL = processTemplate(parsed.sql, context.toMap)
    
    debug(s"populated SQL:\n${populatedSQL}")

    SQLTemplate(
      sql = parsed.sql,
      populated = populatedSQL,
      params = params,
      imports = parsed.imports,
      optionals = parsed.optionals
    )
  }
  
  private def processTemplate(template: String, context: Map[String, Any]): String = {
    // Pattern to match ${...} expressions
    val pattern = """\$\{([^}]+)\}""".r
    
    pattern.replaceAllIn(template, m => {
      val expr = m.group(1).trim
      evaluateExpression(expr, context)
    })
  }
  
  private def evaluateExpression(expr: String, context: Map[String, Any]): String = {
    // Handle various expression patterns
    expr match {
      // Direct parameter reference
      case param if context.contains(param) => 
        context(param).toString
        
      // String literal
      case s if s.startsWith("\"") && s.endsWith("\"") => 
        s.substring(1, s.length - 1)
        
      // Number literal
      case n if n.matches("-?\\d+(\\.\\d+)?[LlFfDd]?") => 
        n
        
      // Boolean literal
      case "true" | "false" => 
        expr
        
      // Simple method calls
      case methodCall if methodCall.contains(".") && !methodCall.contains("(") =>
        val parts = methodCall.split("\\.", 2)
        val objName = parts(0).trim
        val methodName = parts(1).trim
        
        context.get(objName) match {
          case Some(value: String) =>
            methodName match {
              case "toUpperCase" => value.toUpperCase
              case "toLowerCase" => value.toLowerCase
              case "trim" => value.trim
              case "length" => value.length.toString
              case _ => 
                warn(s"Unsupported method '$methodName' on String in Scala 3 SQL template")
                s"${value}"
            }
          case Some(value) if methodName == "toString" =>
            value.toString
          case _ =>
            warn(s"Cannot evaluate expression '$expr' in Scala 3 SQL template")
            s"(${expr})"
        }
        
      // For complex expressions that require runtime compilation
      case _ =>
        warn(s"Complex expression '$expr' cannot be evaluated at compile time in Scala 3. " +
             s"Consider using simpler expressions or default parameter values.")
        // Return the expression as-is for now
        s"(${expr})"
    }
  }
}