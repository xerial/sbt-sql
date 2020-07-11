package example

import java.sql.DriverManager

object ReadSQLTest
        extends wvlet.airspec.AirSpec
{

  Class.forName("org.sqlite.JDBC")

  implicit val connection = DriverManager.getConnection("jdbc:sqlite::memory:")

  private def execute(sql: String): Unit =
  {
    val stmt = connection.createStatement()
    try {
      stmt.execute(sql)
    }
    finally {
      stmt.close()
    }
  }

  override protected def beforeAll: Unit =
  {
    execute(s"create table person(id string, name string)")
    execute(s"insert into person values ('1', 'leo'), ('2', 'yui'), ('3', null)")
  }

  override protected def afterAll: Unit =
  {
    connection.close()
  }

  test("show original SQL") {
    val sql = person.sql()
    assert(sql == "select * from person")
  }

  test("select()") {
    val result = person.select()
    assert(result == Seq(person("1", "leo"), person("2", "yui"), person("3", "")))
  }

  test("selectWith(SQL)") {
    // select with sql
    val r1 = person.selectWith("select * from person where id = '2'")
    info(s"person.selectWith(...): ${r1}")
    assert(r1 == Seq(person("2", "yui")))
  }

  test("selectStream") {
    // Reading with stream
    info(s"person.selectStream()")
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
      info(Seq(r0, r1, r2))
      assert(it.hasNext == false)
    }
  }

  test("selectStream and map") {
    person.selectStream() { it =>
      val result = it.map(_.toString).mkString(",")
      info(s"person.selectStream(): ${result}")
      assert(result == """person(1,leo),person(2,yui),person(3,)""")
    }
  }

  test("selectStreamWith(SQL)") {
    person.selectStreamWith("select * from person where id = '1'") { it =>
      assert(it.hasNext)
      val r = it.next()
      info(r)
      assert(r == person("1", "leo"))
      assert(it.hasNext == false)
    }
  }

  test("generate case class with xxx__optional column") {
    val result2 = person_opt.select()
    info(s"person_opt.select(): ${result2}")
    assert(result2 == Seq(person_opt("1", Some("leo")), person_opt("2", Some("yui")), person_opt("3", None)))
  }

  test("generate case class with @optional column") {
    val result3 = person_opt2.select()
    info(s"person_opt2.select(): ${result3}")
    assert(result3 == Seq(person_opt2("1", Some("leo")), person_opt2("2", Some("yui")), person_opt2("3", None)))
  }
}
