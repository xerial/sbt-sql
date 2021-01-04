package xerial.sbt.sql

import java.util.Locale

import wvlet.log.LogSupport

import scala.util.parsing.combinator.RegexParsers

/**
  * Parse JDBC Type Names (based on Presto's types)
  * This class defines mapping between Presto data type.
  *
  * Presto types: https://trino.io/docs/current/language/types.html
  */
object JDBCTypeNameParser extends RegexParsers with LogSupport {

  import xerial.sbt.sql.DataType._

  private def typeName: Parser[String] = "[a-z][a-z ]*".r

  private def number: Parser[Int] =
    "[0-9]*".r ^^ {
      _.toInt
    }

  private def primitiveType: Parser[DataType] =
    typeName ^^ {
      toScalaPrimitiveType(_)
    }

  private def varcharType: Parser[DataType] =
    "varchar" ~ opt("(" ~ number ~ ")") ^^ {
      case _ => StringType
    }

  private def decimalType: Parser[DecimalType] =
    "decimal" ~ "(" ~ number ~ "," ~ number ~ ")" ^^ {
      case _ ~ _ ~ p ~ _ ~ s ~ _ =>
        DecimalType(p, s)
    }

  private def arrayType: Parser[ArrayType] =
    "array" ~ "(" ~ dataType ~ ")" ^^ {
      case _ ~ _ ~ x ~ _ => ArrayType(x)
    }

  private def mapType: Parser[DataType] =
    "map" ~ "(" ~ dataType ~ "," ~ dataType ~ ")" ^^ {
      case _ ~ _ ~ k ~ _ ~ v ~ _ =>
        // Reading map type is not supported in JdbcUtil of Spark, so use String instead
        MapType(k, v)
    }

  private def dataType: Parser[DataType] =
    varcharType | decimalType | arrayType | mapType | primitiveType

  def parseDataType(s: String): Option[DataType] = {
    val input = s.toLowerCase(Locale.US).trim
    parseAll(dataType, input) match {
      case Success(result, next) => Some(result)
      case Error(msg, next) =>
        warn(msg)
        None
      case Failure(msg, next) =>
        warn(msg)
        None
    }
  }

  //  // See also https://github.com/embulk/embulk-input-jdbc/blob/9ce3e5528a205f86e9c2892dd8a3739f685e07e7/embulk-input-jdbc/src/main/java/org/embulk/input/jdbc/getter/ColumnGetterFactory.java#L92
  //  val default : java.sql.JDBCType => DataType = {
  //    case BIT | BOOLEAN => BooleanType
  //
  //    case TINYINT | SMALLINT => IntType
  //    case INTEGER | BIGINT => LongType
  //
  //    case FLOAT | REAL  => FloatType
  //    case DOUBLE => DoubleType
  //
  //    case NUMERIC | DECIMAL => StringType // TODO
  //    case CHAR | VARCHAR | LONGVARCHAR | CLOB | NCHAR | NVARCHAR => StringType
  //    case DATE => StringType // TODO
  //    case ARRAY => ArrayType(AnyType) // TODO
  //    case _ => StringType
  //  }

  private def toScalaPrimitiveType(typeName: String): DataType = {
    typeName match {
      case "bit" | "boolean"              => BooleanType
      case "tinyint"                      => ByteType
      case "smallint"                     => ShortType
      case "integer"                      => IntType
      case "bigint" | "long"              => LongType
      case "float" | "real"               => FloatType
      case "double"                       => DoubleType
      case "date"                         => DateType
      case "json"                         => StringType
      case "char"                         => StringType
      case "numeric" | "decimal"          => StringType // TODO
      case t if t.startsWith("interval ") => StringType
      case "time" | "time with time zone" =>
        // Return string to be compatible with TD API
        StringType
      case "timestamp" | "timestamp with time zone" =>
        // Return strings since java.sql.timestamp can't hold timezone information
        StringType
      case "varbinary" =>
        BinaryType
      case unknown =>
        // Use StringType for all unknown types
        StringType
    }
  }
}
