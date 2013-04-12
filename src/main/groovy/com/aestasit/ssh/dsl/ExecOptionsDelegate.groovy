package com.aestasit.ssh.dsl

import com.aestasit.ssh.ExecOptions

/**
 * Closure delegate that is used to collect all EXEC (remote command execution) options including command itself.
 *
 * @author Andrey Adamovich
 *
 */
public class ExecOptionsDelegate extends ExecOptions {

  def String command

  def getExecOptions() {
    return this
  }
}