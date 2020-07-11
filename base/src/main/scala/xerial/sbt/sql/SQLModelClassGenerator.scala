package xerial.sbt.sql

import java.sql.JDBCType
import java.util.Properties

import sbt.{File, IO, _}
import wvlet.log.LogSupport
import xerial.sbt.sql.DataType.StringType

import scala.util.{Failure, Success, Try}
import java.sql.ResultSetMetaData

case class Schema(columns: Seq[Column])

case class Column(
    qname: String,
    reader: DataType,
    sqlType: java.sql.JDBCType,
    isNullable: Boolean,
    elementType: java.sql.JDBCType = JDBCType.NULL
)

case class GeneratorConfig(sqlDir: File, targetDir: File)

object SQLModelClassGenerator extends LogSupport {

  private lazy val buildProps = {
    val p  = new Properties()
    val in = this.getClass.getResourceAsStream("/org/xerial/sbt/sbt-sql/build.properties")
    if (in != null) {
      try {
        p.load(in)
      } finally {
        in.close
      }
    } else {
      warn("build.properties file not found")
    }
    p
  }

  lazy val getBuildTime: Long = {
    buildProps.getProperty("build_time", System.currentTimeMillis().toString).toLong
  }
  lazy val getVersion: String = {
    buildProps.getProperty("version", "unknown")
  }

  case class JDBCResultColumn(name: String, typeName: String, typeId: Int, isNullable: Boolean)

  private[sql] def generateSchema(columns: Seq[JDBCResultColumn], optionalColumns: Set[String]): Schema = {
    val colTypes = columns.map { c =>
      val tpe              = JDBCType.valueOf(c.typeId)
      val typeName         = c.typeName
      val originalDataType = JDBCTypeNameParser.parseDataType(typeName).getOrElse(StringType)
      val (colName, dataType) = if (c.name.endsWith("__optional") || optionalColumns.contains(c.name)) {
        (c.name.stripSuffix("__optional"), DataType.OptionType(originalDataType))
      } else {
        (c.name, originalDataType)
      }
      val qname = colName match {
        // Scala's reserved keywords
        case "type" => "`type`"
        case other  => other
      }
      Column(qname, dataType, tpe, c.isNullable)
    }
    Schema(colTypes.toIndexedSeq)
  }
}

class SQLModelClassGenerator(jdbcConfig: JDBCConfig) extends LogSupport {
  import SQLModelClassGenerator._

  wvlet.log.Logger.init

  private val db = new JDBCClient(jdbcConfig)

  private def wrapWithLimit0(sql: String) = {
    s"""-- sbt-sql version:${SQLModelClassGenerator.getVersion}
       |SELECT * FROM (
       |${sql.trim}
       |) LIMIT 0""".stripMargin
  }

  def checkResultSchema(sql: String, optionalParams: Set[String]): Schema = {
    db.withConnection { conn =>
      db.submitQuery(conn, sql) { rs =>
        val m = rs.getMetaData
        val cols = (1 to m.getColumnCount).map { i =>
          JDBCResultColumn(
            m.getColumnName(i),
            m.getColumnTypeName(i),
            m.getColumnType(i),
            m.isNullable(i) == ResultSetMetaData.columnNullable
          )
        }
        // Ensure materializing cols before closing the connection
        generateSchema(cols.toIndexedSeq, optionalParams)
      }
    }
  }

  def generate(config: GeneratorConfig): Seq[File] = {
    // Submit queries using multi-threads to minimize the waiting time
    val result    = Seq.newBuilder[File]
    val buildTime = SQLModelClassGenerator.getBuildTime
    debug(s"SQLModelClassGenerator version:${SQLModelClassGenerator.getVersion}")

    val baseDir = file(".")

    for (sqlFile <- (config.sqlDir ** "*.sql").get.par) {
      val path            = sqlFile.relativeTo(config.sqlDir).get.getPath
      val targetClassFile = config.targetDir / path.replaceAll("\\.sql$", ".scala")

      val sqlFilePath = sqlFile.relativeTo(baseDir).getOrElse(sqlFile)
      debug(s"Processing ${sqlFilePath}")
      val latestTimestamp = Math.max(sqlFile.lastModified(), buildTime)
      if (
        targetClassFile.exists()
        && targetClassFile.exists()
        && latestTimestamp <= targetClassFile.lastModified()
      ) {
        debug(s"${targetClassFile.relativeTo(config.targetDir).getOrElse(targetClassFile)} is up-to-date")
      } else {
        val sql = IO.read(sqlFile)
        Try(SQLTemplate(sql)) match {
          case Success(template) =>
            val limit0 = wrapWithLimit0(template.populated)
            info(s"Checking the SQL result schema of ${sqlFilePath}")
            val schema = checkResultSchema(limit0, template.optionalParams)

            // Write SQL template without type annotation
            val scalaCode = schemaToClass(sqlFile, config.sqlDir, schema, template)
            info(s"Generating model class: ${targetClassFile}")
            info(s"${scalaCode}")
            IO.write(targetClassFile, scalaCode)
            targetClassFile.setLastModified(latestTimestamp)
          case Failure(e) =>
            error(s"Failed to parse ${sqlFile}: ${e.getMessage}")
            throw e
        }
      }

      synchronized {
        result += targetClassFile
      }
    }
    result.result()
  }

  def schemaToParamDef(schema: Schema) = {
    schema.columns.map { c =>
      s"${c.qname}: ${c.reader.name}"
    }
  }

  def schemaToPackerCode(schema: Schema, packerName: String = "packer") = {
    for (c <- schema.columns) {
      s"${packerName}.packXXX(${c.qname})"
    }
  }

  def schemaToClass(origFile: File, baseDir: File, schema: Schema, sqlTemplate: SQLTemplate): String = {
    val packageName = origFile
      .relativeTo(baseDir).map { f =>
        Option(f.getParent).map(_.replaceAll("""[\\/]""", ".")).getOrElse("")
      }.getOrElse("")
    val name = origFile.getName.replaceAll("\\.sql$", "")

    val params = schemaToParamDef(schema)

    val sqlTemplateArgs = sqlTemplate.params.map { p =>
      p.defaultValue match {
        case None => s"${p.name}:${p.functionArgType}"
        case Some(v) => s"${p.name}:${p.functionArgType} = ${p.quotedValue}"
      }
    }
    val sqlArgList = sqlTemplateArgs.mkString(", ")
    val paramNames = sqlTemplate.params.map(_.name)

    val additionalImports = sqlTemplate.imports.map(x => s"import ${x.target}").mkString("\n")
    val embeddedSQL = "\"\"\"" + sqlTemplate.sql + "\"\"\""

    val packageLine = if (packageName.isEmpty) {
      ""
    }
    else {
      s"package ${packageName}"
    }
    val code =
      s"""/**
         | * DO NOT EDIT THIS FILE. This file is generated by sbt-sql
         | */
         |${packageLine}
         |import wvlet.airframe.codec._
         |import wvlet.airframe.control.Control.withResource
         |
         |${additionalImports}
         |
         |object ${name} extends wvlet.log.LogSupport {
         |  def path : String = "/${packageName.replaceAll("\\.", "/")}/${name}.sql"
         |
         |  private lazy val codec = MessageCodec.of[${name}]
         |
         |  def sql(${sqlArgList}) : String = {
         |    s${embeddedSQL}
         |  }
         |
         |  def select(${sqlArgList})(implicit conn:java.sql.Connection): Seq[${name}] = {
         |    selectWith(sql(${paramNames.mkString(", ")}))(conn)
         |  }
         |
         |  def selectWith(sql:String)(implicit conn:java.sql.Connection) : Seq[${name}] = {
         |    selectStreamWith(sql:String){ it =>
         |      it.toIndexedSeq
         |    }(conn)
         |  }
         |
         |  def selectStream[R](${sqlArgList})
         |    (streamReader: scala.collection.Iterator[${name}] => R)
         |    (implicit conn:java.sql.Connection) : R = {
         |    selectStreamWith(sql(${paramNames.mkString(", ")}))(streamReader)(conn)
         |  }
         |
         |  def selectStreamWith[R](sql:String)
         |    (streamReader: scala.collection.Iterator[${name}] => R)
         |    (implicit conn:java.sql.Connection) : R = {
         |    withResource(conn.createStatement()) { stmt =>
         |      debug(s"Executing query:\\n$${sql}")
         |      withResource(stmt.executeQuery(sql)) { rs =>
         |        val jdbcCodec = JDBCCodec(rs)
         |        val it = jdbcCodec.mapMsgPackArrayRows{ msgpack =>
         |          codec.fromMsgPack(msgpack)
         |        }
         |        streamReader(it.toIterator)
         |      }
         |    }
         |  }
         |}
         |
         |case class ${name}(
         |  ${params.mkString(",\n  ")}
         |)
         |""".stripMargin

    code
  }

}
