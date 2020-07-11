package example

import java.sql.DriverManager
import wvlet.airframe.control.Control._

object ReadSQL
{
  Class.forName("org.sqlite.JDBC")
  implicit val connection = DriverManager.getConnection("jdbc:sqlite::memory:")

  private def execute(sql: String): Unit =
  {
    withResource(connection.createStatement()) { stmt =>
      stmt.execute(sql)
    }
  }

  def main(args: Array[String]): Unit =
  {
    try {
      execute(s"create table person(id string, name string)")
      execute(s"insert into person values ('1', 'leo'), ('2', 'yui'), ('3', null)")
      val sql = person.sql()
      assert(sql == "select * from person")
      val result = person.select()
      println(result)
      assert(result == Seq(person("1", "leo"), person("2", "yui"), person("3", "")))

      // select with sql
      val r1 = person.selectWith("select * from person where id = '2'")
      assert(r1 == Seq(person("2", "yui")))

      // Reading with stream
      person.selectStream() { it =>
        assert(it.hasNext)
        val r0 = it.next()
        assert(r0 == person("1", "leo"))
        assert(it.hasNext)
        val r1 = it.next()
        assert(r1 == person("2", "yui"))
        assert(it.hasNext)
        val r2 = it.next()
        assert(r2 == person("3", ""))
        println(Seq(r0, r1, r2))
        assert(it.hasNext == false)
      }

      person.selectStreamWith("select * from person where id = '1'") { it =>
        assert(it.hasNext)
        val r = it.next()
        println(r)
        assert(r == person("1", "leo"))
        assert(it.hasNext == false)
      }

      val result2 = person_opt.select()
      assert(result2 == Seq(person_opt("1", Some("leo")), person_opt("2", Some("yui")), person_opt("3", None)))

      val result3 = person_opt2.select()
      assert(result3 == Seq(person_opt2("1", Some("leo")), person_opt2("2", Some("yui")), person_opt2("3", None)))
    }
    finally {
      connection.close()
    }
  }
}
