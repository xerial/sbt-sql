package xerial.sbt.sql

import xerial.core.log.LogLevel

/**
  *
  */
trait LogSupport {
  def error(s: String)
  def warn(s: String)
  def info(s: String)
  def debug(s: String)
}

class SbtLogSupport(l: sbt.Logger) extends LogSupport {
  override def error(s: String): Unit = l.error(s)
  override def warn(s: String): Unit = l.warn(s)
  override def info(s: String): Unit = l.info(s)
  override def debug(s: String): Unit = l.debug(s)
}

class DebugLogSupport(l: xerial.core.log.LogWriter) extends LogSupport {
  override def error(s: String): Unit = l.log(LogLevel.ERROR, s)
  override def warn(s: String): Unit = l.log(LogLevel.WARN, s)
  override def info(s: String): Unit = l.log(LogLevel.INFO, s)
  override def debug(s: String): Unit = l.log(LogLevel.TRACE, s)
}

