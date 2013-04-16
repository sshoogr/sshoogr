package com.aestasit.ssh.log

import com.jcraft.jsch.SftpProgressMonitor


/**
 * File coping progress monitor that prints progress status using logging system.
 *
 * @author Andrey Adamovich
 *
 */
class LoggerProgressMonitor implements SftpProgressMonitor {

  private final Logger logger
  private int max = 1
  private int current = 0

  LoggerProgressMonitor(Logger logger) {
    this.logger = logger
  }

  void init(int op, String src, String dest, long max) {
    this.max = max
    this.current = 0
  }

  boolean count(long count) {
    current += count
    // int percent = (current / max * 100) as int
    // TODO: logging settings
    logger.info("${current} bytes transfered")
    true
  }

  void end() {
  }
}