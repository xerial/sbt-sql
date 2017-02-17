package xerial.sbt.sql

import java.sql.JDBCType._

abstract class ColumnAccess(val name:String, val rsMethod:String)
case object BooleanColumn extends ColumnAccess("Boolean", "getBoolean")
case object IntColumn extends ColumnAccess("Int", "getInt")
case object LongColumn extends ColumnAccess("Long", "getLong")
case object FloatColumn extends ColumnAccess("Float", "getFloat")
case object DoubleColumn extends ColumnAccess("Double", "getDouble")
case object StringColumn extends ColumnAccess("String", "getString")
case object ArrayColumn extends ColumnAccess("java.sql.Array", "getArray")
case object MapColumn extends ColumnAccess("AnyRef", "getObject")

/**
  *
  */
object SQLTypeMapping {

  // See also https://github.com/embulk/embulk-input-jdbc/blob/9ce3e5528a205f86e9c2892dd8a3739f685e07e7/embulk-input-jdbc/src/main/java/org/embulk/input/jdbc/getter/ColumnGetterFactory.java#L92
  val default : java.sql.JDBCType => ColumnAccess = {
    case BIT | BOOLEAN => BooleanColumn

    case TINYINT | SMALLINT => IntColumn
    case INTEGER | BIGINT => LongColumn

    case FLOAT | REAL  => FloatColumn
    case DOUBLE => DoubleColumn

    case NUMERIC | DECIMAL => StringColumn // TODO
    case CHAR | VARCHAR | LONGVARCHAR | CLOB | NCHAR | NVARCHAR => StringColumn
    case DATE => StringColumn // TODO
    case ARRAY => ArrayColumn
    case _ => StringColumn
  }

}
