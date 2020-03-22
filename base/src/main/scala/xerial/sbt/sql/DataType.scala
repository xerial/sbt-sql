package xerial.sbt.sql

/**
 *
 */
object DataType
{

  case object BooleanType
          extends DataType("Boolean")

  case object ByteType
          extends DataType("Byte")

  case object ShortType
          extends DataType("Short")

  case object IntType
          extends DataType("Int")

  case object DateType
          extends DataType("java.sql.Date")

  case object LongType
          extends DataType("Long")

  case object FloatType
          extends DataType("Float")

  case object DoubleType
          extends DataType("Double")

  case object StringType
          extends DataType("String")

  case object BinaryType
          extends DataType(name = "Array[Byte]")

  case class DecimalType(p: Int, s: Int)
          extends DataType(name = "")

  case class ArrayType(elementType: DataType)
          extends DataType(s"Array[${elementType}}]", Seq(elementType))

  case class MapType(keyType: DataType, valueType: DataType)
          extends DataType(s"Map[${keyType}, ${valueType}]",
            Seq(keyType, valueType))

  case object AnyType
          extends DataType("Any")

}

sealed abstract class DataType(val name: String,
        val typeArgs: Seq[DataType] = Seq.empty)
