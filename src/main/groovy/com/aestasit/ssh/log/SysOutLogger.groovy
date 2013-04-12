package com.aestasit.ssh.log

class SysOutLogger implements Logger {

  public void info(String message) {
    println "$message"    
  }

  @Override
  public void warn(String message) {
    println "WARN: $message"    
  }

  @Override
  public void debug(String message) {
    println "DEBUG: $message"    
  }

}
