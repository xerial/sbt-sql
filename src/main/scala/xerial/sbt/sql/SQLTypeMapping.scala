package xerial.sbt.sql

import java.sql.JDBCType._

/**
  *
  */
object SQLTypeMapping {


  // See also https://github.com/embulk/embulk-input-jdbc/blob/9ce3e5528a205f86e9c2892dd8a3739f685e07e7/embulk-input-jdbc/src/main/java/org/embulk/input/jdbc/getter/ColumnGetterFactory.java#L92
  val default : java.sql.JDBCType => String = {
    case BIT | BOOLEAN => "Boolean"

    case TINYINT | SMALLINT => "Int"
    case INTEGER | BIGINT => "Long"

    case FLOAT | REAL  => "Float"
    case DOUBLE => "Double"

    case NUMERIC | DECIMAL => "String" // TODO
    case CHAR | VARCHAR | LONGVARCHAR | CLOB | NCHAR | NVARCHAR => "String"
    case DATE => "String" // TODO
    case ARRAY => "org.msgpack.value.ArrayValue"
    case _ => "String"
  }

}
