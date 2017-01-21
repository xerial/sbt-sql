package xerial.sbt.sql

import xerial.core.log.Logger

/**
  *
  */
object SQLTemplate extends Logger {
  case class TemplateParam(name:String, typeName:String)

  val embeddedParamPattern = """\$\{\s*(\w+)\s*(:\s*(\w+))?\s*\}""".r

  def extractParam(sql:String) : Seq[TemplateParam] = {
    // TODO remove comment lines
    info(sql)
    val params = Seq.newBuilder[TemplateParam]
    for(m <- embeddedParamPattern.findAllMatchIn(sql)) {
      val name = m.group(1)
      val typeName = Option(m.group(3))
      params += TemplateParam(name, typeName.getOrElse("String"))
    }
    params.result()
  }

}
