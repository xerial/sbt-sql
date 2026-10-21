package xerial.sbt.sql

import wvlet.airframe.surface.Zero
import wvlet.airframe.surface.reflect.ReflectSurfaceFactory

import scala.util.Try

/** Evaluates Scala code of SQL templates at build time using the Scala 2 ToolBox */
private[sql] object TemplateEval {

  def zeroOf(typeName: String): Any =
    Try(Zero.zeroOf(ReflectSurfaceFactory.ofClass(Class.forName(typeName)))).toOption.getOrElse(null)

  def eval(code: String): Any = {
    import scala.reflect.runtime.currentMirror
    import scala.tools.reflect.ToolBox
    val toolBox = currentMirror.mkToolBox()
    toolBox.eval(toolBox.parse(code))
  }
}
