@import java.util.Locale
@(id:Int, name:String="LEO")
select * from person where id = '${id}' or name = '${name.toLowerCase(Locale.ROOT)}'
