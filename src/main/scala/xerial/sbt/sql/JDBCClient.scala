package xerial.sbt.sql

import java.sql.{Connection, DriverManager, ResultSet}

import xerial.core.log.Logger

private[sql] case class JDBCConfig(
  driver: String,
  url: String,
  user: String,
  password: String
)

/**
  *
  */
class JDBCClient(config:JDBCConfig) extends Logger {
  private def withResource[R <: AutoCloseable, U](r: R)(body: R => U): U = {
    try {
      body(r)
    }
    finally {
      r.close()
    }
  }

  def withConnection[U](body: Connection => U) : U = {
    Class.forName(config.driver)
    withResource(DriverManager.getConnection(config.url, config.user, config.password)) {conn =>
      body(conn)
    }
  }

  def submitQuery[U](conn:Connection, sql: String)(body: ResultSet => U): U = {
    withResource(conn.createStatement()) {stmt =>
      info(s"running sql:\n${sql}")
      withResource(stmt.executeQuery(sql)) {rs =>
        body(rs)
      }
    }
  }

}
