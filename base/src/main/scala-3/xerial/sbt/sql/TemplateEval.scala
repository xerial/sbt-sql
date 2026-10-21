package xerial.sbt.sql

import dotty.tools.dotc.Driver
import dotty.tools.dotc.reporting.StoreReporter

import java.io.File
import java.net.URLClassLoader
import java.nio.file.{Files, Path}
import scala.jdk.CollectionConverters._
import scala.util.Using

/** Evaluates Scala code of SQL templates at build time using the Scala 3 compiler, as Scala 3 has no ToolBox */
private[sql] object TemplateEval {

  // Scala 3 has no runtime reflection to build zero values of arbitrary classes
  def zeroOf(typeName: String): Any = null

  private val evalClassName = "SbtSQLTemplateEval"

  def eval(code: String): Any = {
    val workDir = Files.createTempDirectory("sbt-sql-eval")
    try {
      val src = workDir.resolve(s"${evalClassName}.scala")
      Files.writeString(
        src,
        s"""class ${evalClassName} extends (() => Any) {
           |  def apply(): Any = {
           |${code}
           |  }
           |}
           |""".stripMargin
      )
      val outDir = Files.createDirectory(workDir.resolve("classes"))
      // Collect compile errors to report them through the exception instead of printing them to stdout
      val reporter = new StoreReporter()
      new Driver().process(
        Array("-classpath", classpath, "-d", outDir.toString, "-nowarn", src.toString),
        reporter
      )
      if (reporter.hasErrors) {
        val errors = reporter.allErrors
        throw new IllegalArgumentException(
          s"Failed to compile SQL template code:\n${errors.map(_.message).mkString("\n")}\n${code}"
        )
      }
      // Load all generated classes in memory so that the temporary directory can be removed
      val loader = new InMemoryClassLoader(readClasses(outDir), getClass.getClassLoader)
      loader.loadClass(evalClassName).getDeclaredConstructor().newInstance().asInstanceOf[() => Any].apply()
    } finally {
      sbt.IO.delete(workDir.toFile)
    }
  }

  // The plugin runs inside sbt, so java.class.path doesn't contain the Scala library
  private lazy val classpath: String = {
    def location(className: String): Seq[String] =
      Option(Class.forName(className).getProtectionDomain.getCodeSource)
        .map(s => new File(s.getLocation.toURI).getPath).toSeq
    def urls(cl: ClassLoader): Seq[String] = cl match {
      case null              => Nil
      case u: URLClassLoader => u.getURLs.toSeq.map(u => new File(u.toURI).getPath) ++ urls(u.getParent)
      case other             => urls(other.getParent)
    }
    // Scala 2.13 and Scala 3 parts of the standard library
    val libs = location("scala.Function0") ++ location("scala.util.boundary$")
    (libs ++ urls(getClass.getClassLoader)).distinct.mkString(File.pathSeparator)
  }

  private def readClasses(dir: Path): Map[String, Array[Byte]] =
    Using.resource(Files.walk(dir)) { paths =>
      paths.iterator.asScala
        .filter(_.toString.endsWith(".class"))
        .map { p =>
          val name = dir.relativize(p).toString.stripSuffix(".class").replace(File.separatorChar, '.')
          name -> Files.readAllBytes(p)
        }.toMap
    }

  private class InMemoryClassLoader(classes: Map[String, Array[Byte]], parent: ClassLoader)
      extends ClassLoader(parent) {
    override protected def findClass(name: String) =
      classes.get(name) match {
        case Some(bytes) => defineClass(name, bytes, 0, bytes.length)
        case None        => super.findClass(name)
      }
  }
}
