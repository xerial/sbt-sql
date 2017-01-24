package xerial.sbt.sql

import sbt._
import java.sql.{Connection, DriverManager, JDBCType, ResultSet}

import sbt.{File, IO}

case class Schema(columns: Seq[Column])
case class Column(name: String, sqlType: java.sql.JDBCType, isNullable: Boolean)

class SQLModelClassGenerator(config: JDBCConfig) extends xerial.core.log.Logger {
  private val db = new JDBCClient(config)

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
          val nullable = m.isNullable(i) != 0
          Column(name, jdbcType, nullable)
        }
        Schema(colTypes.toIndexedSeq)
      }
    }
  }

  def generate(sqlDir: File) = {
    // Submit queries using multi-threads to minimize the waiting time
    for (sqlFile <- (sqlDir ** "*.sql").get.par) {
      info(s"Processing ${sqlFile}")
      val sql = IO.read(sqlFile)
      val template = SQLTemplate(sql)
      val limit0 = wrapWithLimit0(template.populated)
      val schema = checkResultSchema(limit0)
      info(s"template:\n${template.noParam}")
      info(schema)
      schemaToClass(sqlFile, sqlDir, schema)
    }
  }

  def schemaToClass(origFile: File, baseDir: File, schema: Schema): String = {
    val packageName = origFile.relativeTo(baseDir).map {f =>
      f.getParent.replaceAll("""[\\/]""", ".")
    }.getOrElse("")
    val name = origFile.getName.replaceAll("\\.sql$", "")

    val params = schema.columns.map {c =>
      val typeClass = SQLTypeMapping.default(c.sqlType)
      s"${c.name}:${typeClass}"
    }

    val code =
      s"""
         |package ${packageName}
         |
         |case class ${name}(
         |  ${params.mkString(",\n  ")}
         |)
        """.stripMargin

    info(code)
    code
  }

}
