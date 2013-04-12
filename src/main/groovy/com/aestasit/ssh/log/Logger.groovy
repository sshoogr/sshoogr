package com.aestasit.ssh.log

interface Logger {

  def void info(String message)
  def void warn(String message)
  def void debug(String message)
  
}
