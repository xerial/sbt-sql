Release Notes 
===

## 0.8
 - Upgrade to Presto 0.186

## 0.7
 - Support sbt-1.0.0-RC3
   - Dropped a support for 0.13.x
 - Support SQLite (sbt-sql-sqlite plugin)

## 0.5
 - Support Scala expression inside `${...}` block
 - Add support for function definition `@(...)` at the sql file header:
 ```
@(v1:String, v2:Int = 0)
select '${v1}', ${v2}
```
 - Support include statement, e.g., `@include java.lang.sql._`

## 0.4
 - Split the plugin into DB specific ones
    - sbt-sql (generic JDBC), sbt-sql-presto, sbt-sql-td (For Treasure Data Presto)
 - Simplified the configuration    

## 0.3
 - Add Treasure Data Presto support
 - Improved log messages

## 0.2
 - Add selectWith
 - Add select(param)(connection)

## 0.1
 - Initial release
