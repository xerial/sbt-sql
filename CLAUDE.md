# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

sbt-sql is an sbt plugin that generates Scala model classes from SQL query files. The plugin integrates SQL with Scala by creating type-safe model classes to read SQL results, eliminating runtime errors from column name mismatches and enabling IDE support for SQL result parameters.

## Key Architecture

### Multi-Module Structure

The project consists of several modules:

- **base**: Core SQL parsing and model generation logic
  - `SQLTemplateParser`: Parses SQL files with template variables
  - `SQLModelClassGenerator`: Generates Scala case classes from SQL schemas
  - `JDBCClient`: Handles JDBC connections and schema retrieval
  
- **generic**: Generic JDBC driver support (sbt-sql)
- **sqlite**: SQLite-specific plugin (sbt-sql-sqlite)
- **duckdb**: DuckDB-specific plugin (sbt-sql-duckdb)
- **trino**: Trino-specific plugin (sbt-sql-trino)
- **td**: Treasure Data-specific plugin (sbt-sql-td)

### SQL File Processing Flow

1. SQL files in `src/main/sql` (or database-specific subdirectories) are parsed
2. Template variables `@(name:type)` and expressions `${expr}` are extracted
3. JDBC connection is used to validate SQL and retrieve result schema
4. Scala model classes are generated in `target/src_managed/main`

## Development Commands

### Build and Compile
```bash
sbt compile
```

### Run Tests
```bash
sbt test
```

### Run Scripted Tests (Integration Tests)
```bash
sbt scripted
```

### Run a Specific Scripted Test
```bash
sbt "scripted sbt-sql-sqlite/basic"
```

### Format Code
```bash
sbt scalafmtAll
```

### Clean Build
```bash
sbt clean
```

### Publish Local (for testing plugin changes)
```bash
sbt publishLocal
```

### Update Plugin Version in Test Projects
```bash
sbt bumpPluginVersion
```

## Plugin Development Tips

When modifying the plugin:

1. The main plugin logic is in the `base` module under `xerial.sbt.sql`
2. Database-specific plugins extend `SbtSQLBase` from the base module
3. SQL template parsing uses Scala parser combinators
4. Generated code uses Airframe Surface for JDBC-to-Scala mapping
5. Scripted tests in `src/sbt-test` verify plugin functionality

## Testing Plugin Changes

To test changes:
1. Run `sbt publishLocal` to publish the plugin locally
2. Update the plugin version in test projects if needed
3. Run `sbt scripted` to execute integration tests