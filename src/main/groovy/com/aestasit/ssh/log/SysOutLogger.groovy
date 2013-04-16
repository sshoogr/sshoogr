package com.aestasit.ssh.log

/**
 * Standard system output logger.
 *
 * @author Andrey Adamovich
 *
 */
class SysOutLogger implements Logger {

  def void info(String message) {
    println "$message"
  }

  def void warn(String message) {
    println "WARN: $message"
  }

  def void debug(String message) {
    println "DEBUG: $message"
  }
}
