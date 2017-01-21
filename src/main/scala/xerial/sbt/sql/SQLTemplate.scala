package xerial.sbt.sql

import xerial.core.log.Logger

import scala.io.Source

/**
  *
  */
object SQLTemplate extends Logger {
  case class TemplateParam(name:String, typeName:String, line:Int, start:Int, end:Int)

  val embeddedParamPattern = """\$\{\s*(\w+)\s*(:\s*(\w+))?\s*\}""".r

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

}
