package com.aestasit.ssh

import com.aestasit.ssh.log.Logger


/**
 * Configuration object holding options used for global SSH plug-in configuration.
 *
 * @author Andrey Adamovich
 *
 */
class SshOptions extends CommonOptions {

  // SSH connection options.
  String defaultHost               = null
  String defaultUser               = null
  String defaultPassword           = null
  int defaultPort                  = 22
  boolean trustUnknownHosts        = false
  boolean reuseConnection          = false
  File defaultKeyFile              = null
  Logger logger                    = null

  // SSH command execution options.
  ExecOptions execOptions          = new ExecOptions()

  def execOptions(Closure cl) {
    cl.delegate = execOptions
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
  }

  // SCP options.
  ScpOptions scpOptions   = new ScpOptions()

  def scpOptions(Closure cl) {
    cl.delegate = scpOptions
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
  }

}
