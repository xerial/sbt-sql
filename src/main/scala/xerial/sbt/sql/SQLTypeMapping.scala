package xerial.sbt.sql

import java.sql.JDBCType._

abstract class ColumnReader(val name:String, val rsMethod:String)
case object BooleanColumn extends ColumnReader("Boolean", "getBoolean")
case object IntColumn extends ColumnReader("Int", "getInt")
case object LongColumn extends ColumnReader("Long", "getLong")
case object FloatColumn extends ColumnReader("Float", "getFloat")
case object DoubleColumn extends ColumnReader("Double", "getDouble")
case object StringColumn extends ColumnReader("String", "getString")
case object ArrayColumn extends ColumnReader("org.msgpack.value.ArrayValue", "getObject")
case object MapColumn extends ColumnReader("org.msgpack.value.MapValue", "getObject")

/**
  *
  */
object SQLTypeMapping {

  // See also https://github.com/embulk/embulk-input-jdbc/blob/9ce3e5528a205f86e9c2892dd8a3739f685e07e7/embulk-input-jdbc/src/main/java/org/embulk/input/jdbc/getter/ColumnGetterFactory.java#L92
  val default : java.sql.JDBCType => ColumnReader = {
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
