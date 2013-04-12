package com.aestasit.ssh.log

class Slf4jLogger implements Logger {

  private org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(getClass().getPackage().getName())

  public void info(String message) {
    logger.info(message)
  }

  public void warn(String message) {
    logger.warn(message)
  }

  public void debug(String message) {
    logger.debug(message)
  }
}
