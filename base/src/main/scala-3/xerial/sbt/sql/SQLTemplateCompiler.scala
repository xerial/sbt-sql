package xerial.sbt.sql

import wvlet.airframe.surface.{Surface, Zero}
import wvlet.airframe.surface.reflect.{ReflectSurfaceFactory, ReflectTypeUtil}
import wvlet.log.LogSupport

import scala.util.{Failure, Success, Try}

/** 
 * Scala 3 version of SQLTemplateCompiler
 * Since runtime reflection is not available in Scala 3, this version
 * generates SQL with placeholders that will be filled at runtime
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
        Try(Zero.zeroOf(ReflectSurfaceFactory.ofTypeName(typeName))).toOption.getOrElse(null)
    }

  def compile(sqlTemplate: String): SQLTemplate = {
    val parsed  = SQLTemplateParser.parse(sqlTemplate)
    val params  = parsed.args
    val imports = parsed.imports.map(x => s"import ${x.target}").mkString("\n")

    val (defaultParams, otherParams) = params.partition(_.defaultValue.isDefined)

    // For Scala 3, we generate SQL with placeholders ${param_name} that will be replaced at runtime
    // This is a workaround for the lack of runtime compilation
    
    // Create a map of default values
    val defaultValues = defaultParams.map(p => p.name -> p.defaultValue.get).toMap
    val otherValues = otherParams.map(p => p.name -> defaultValueFor(p.typeName)).toMap
    val allValues = defaultValues ++ otherValues
    
    // Process the SQL template
    var populatedSQL = parsed.sql.sql
    
    // Replace embedded expressions with their values or placeholders
    parsed.sql.embeddedExpressions.foreach { expr =>
      val exprCode = expr.code.trim
      val replacement = evaluateSimpleExpression(exprCode, allValues)
      populatedSQL = populatedSQL.replace(s"$${${expr.code}}", replacement)
    }
    
    debug(s"populated SQL:\n${populatedSQL}")

    new SQLTemplate(
      sql = parsed.sql,
      populated = populatedSQL,
      params = params,
      imports = parsed.imports,
      optionals = parsed.optionals
    )
  }
  
  private def evaluateSimpleExpression(expr: String, values: Map[String, Any]): String = {
    // Handle simple cases that don't require runtime compilation
    expr match {
      // Direct parameter reference
      case param if values.contains(param) => 
        values(param) match {
          case s: String => s
          case v => v.toString
        }
        
      // String literal
      case s if s.startsWith("\"") && s.endsWith("\"") => 
        s.substring(1, s.length - 1)
        
      // SQL type parameter (should be used as-is)
      case param if values.get(param).exists(_ == "") =>
        s"$${$param}"
        
      // For complex expressions, we need to keep them as placeholders
      // These would need to be evaluated at runtime by the generated code
      case _ =>
        warn(s"Complex expression '$expr' cannot be evaluated at compile time in Scala 3. Using placeholder.")
        s"$${$expr}"
    }
  }
}