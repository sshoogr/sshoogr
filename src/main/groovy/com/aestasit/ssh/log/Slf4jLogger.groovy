package com.aestasit.ssh.log

/**
 * Slf4j-based logger.
 *
 * @author Andrey Adamovich
 *
 */
class Slf4jLogger implements Logger {

  private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(getClass().getPackage().getName())

  def void info(String message) {
    logger.info(message)
  }

  def void warn(String message) {
    logger.warn(message)
  }

  def void debug(String message) {
    logger.debug(message)
  }
}
