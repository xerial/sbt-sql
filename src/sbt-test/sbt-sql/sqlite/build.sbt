enablePlugins(SbtSQLJDBC)

name := "sbt-sql-test"

// You can change SQL file folder. The default is src/main/sql
sqlDir := (Compile / sourceDirectory).value / "sql" / "sqlite"

// Configure your JDBC driver
jdbcDriver := "org.sqlite.JDBC"
jdbcURL    := "jdbc:sqlite:sample.db"
