package xerial.sbt.sql

import sbt._
import java.sql.{Connection, DriverManager, JDBCType, ResultSet}
import java.util.Properties

import sbt.{File, IO}

case class Schema(columns: Seq[Column])
case class Column(name: String, reader:ColumnReader, sqlType: java.sql.JDBCType, isNullable: Boolean)

case class GeneratorConfig(sqlDir:File, targetDir:File)

object SQLModelClassGenerator extends xerial.core.log.Logger {

  lazy val getBuildTime : Long = {
    val p = new Properties()
    val in = this.getClass.getResourceAsStream("/org/xerial/sbt/sbt-sql/build.properties")
    if(in != null) {
      try {
        p.load(in)
      }
      finally {
        in.close
      }
    }
    else {
      warn("buid.properties file not found")
    }
    p.getProperty("build_time", System.currentTimeMillis().toString).toLong
  }
}

class SQLModelClassGenerator(jdbcConfig: JDBCConfig) extends xerial.core.log.Logger {
  private val db = new JDBCClient(jdbcConfig)

  protected val typeMapping = SQLTypeMapping.default

  private def wrapWithLimit0(sql: String) = {
    s"""SELECT * FROM (
       |${sql}
       |)
       |LIMIT 0""".stripMargin
  }

  def checkResultSchema(sql: String): Schema = {
    db.withConnection {conn =>
      db.submitQuery(conn, sql) {rs =>
        val m = rs.getMetaData
        val cols = m.getColumnCount
        val colTypes = (1 to cols).map {i =>
          val name = m.getColumnName(i)
          val tpe = m.getColumnType(i)
          val jdbcType = JDBCType.valueOf(tpe)
          val reader = typeMapping(jdbcType)
          val nullable = m.isNullable(i) != 0
          Column(name, reader, jdbcType, nullable)
        }
        Schema(colTypes.toIndexedSeq)
      }
    }
  }


  def generate(config:GeneratorConfig) = {
    // Submit queries using multi-threads to minimize the waiting time
    for (sqlFile <- (config.sqlDir ** "*.sql").get.par) {
      val path = sqlFile.relativeTo(config.sqlDir).get.getPath
      val targetFile = config.targetDir / path
      val targetClassFile = file(targetFile.getPath.replaceAll("\\.sql$", ".scala"))

      info(s"Processing ${sqlFile}")
      val buildTime = SQLModelClassGenerator.getBuildTime
      if(targetFile.exists()
        && targetClassFile.exists()
        && sqlFile.lastModified() < targetFile.lastModified()
        && targetFile.lastModified() > buildTime) {
        info(s"${targetFile} is up-to-date")
      }
      else {
        info(s"Generating ${targetFile}, ${targetClassFile}")
        val sql = IO.read(sqlFile)
        val template = SQLTemplate(sql)
        val limit0 = wrapWithLimit0(template.populated)
        val schema = checkResultSchema(limit0)
        info(s"template:\n${template.noParam}")
        info(schema)

        // Write SQL template without type annotation

        IO.write(targetFile, template.noParam)
        val scalaCode = schemaToClass(sqlFile, config.sqlDir, schema, template)
        IO.write(targetClassFile, scalaCode)
        targetFile.setLastModified(buildTime)
        targetClassFile.setLastModified(buildTime)
      }
    }
  }

  def schemaToClass(origFile: File, baseDir: File, schema: Schema, template:SQLTemplate): String = {
    val packageName = origFile.relativeTo(baseDir).map {f =>
      f.getParent.replaceAll("""[\\/]""", ".")
    }.getOrElse("")
    val name = origFile.getName.replaceAll("\\.sql$", "")

    val params = schema.columns.map {c =>
      s"val ${c.name}:${c.reader.name}"
    }

    val args = template.params.map(p => s"${p.name}:${p.typeName}")
    val paramNames = template.params.map(_.name)

    val rsReader = schema.columns.zipWithIndex.map { case (c, i) =>
      s"rs.${c.reader.rsMethod}(${i+1})"
    }

    val code =
      s"""package ${packageName}
         |import java.sql.ResultSet
         |
         |object ${name} {
         |  def path : String = "/${packageName.replaceAll("\\.", "/")}/${name}.sql"
         |  private def orig : String = {
         |    scala.io.Source.fromInputStream(this.getClass.getResourceAsStream(path)).mkString
         |  }
         |  def apply(rs:ResultSet) : ${name} = {
         |    new ${name}(${rsReader.mkString(", ")})
         |  }
         |  def sql(${args.mkString(", ")}) : String = {
         |    var rendered = orig
         |    val params = Seq(${paramNames.map(x => "\"" + x + "\"").mkString(", ")})
         |    val args = Seq(${paramNames.mkString(", ")})
         |    for((p, arg) <- params.zip(args)) {
         |       rendered = rendered.replaceAll(s"\\$$\\{$${p}\\}", arg.toString)
         |    }
         |    rendered
         |  }
         |}
         |
         |class ${name}(
         |  ${params.mkString(",\n  ")}
         |) {
         |
         |}
         |""".stripMargin

    info(code)
    code
  }

}
