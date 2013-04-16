package com.aestasit.ssh.dsl

/**
 * This class holds data for remote command execution result.
 *
 * @author Andrey Adamovich
 *
 */
class CommandOutput {

  def int exitStatus
  def String output
  def Throwable exception

  CommandOutput(int exitStatus, String output) {
    this.exitStatus = exitStatus
    this.output = output
  }

  CommandOutput(int exitStatus, String output, Throwable exception) {
    this.exitStatus = exitStatus
    this.output = output
    this.exception = exception
  }
}