/*
 * Copyright (C) 2011-2015 Aestas/IT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aestasit.infrastructure.ssh

import com.aestasit.infrastructure.ssh.log.Logger
import com.aestasit.infrastructure.ssh.log.Slf4jLogger
import com.aestasit.infrastructure.ssh.log.SysErrLogger
import com.aestasit.infrastructure.ssh.log.SysOutLogger

import static groovy.lang.Closure.DELEGATE_FIRST

/**
 * Configuration object holding options used for DSL configuration.
 *
 * @author Andrey Adamovich
 *
 */
class SshOptions extends CommonOptions {

  // SSH connection options.
  String defaultHost                 = null
  String defaultUser                 = null
  File defaultKeyFile                = null
  String defaultPassPhrase           = null
  String defaultPassword             = null
  int defaultPort                    = 22
  boolean trustUnknownHosts          = false
  Map<String, String> jschProperties = [:]

  boolean reuseConnection            = false
  boolean verbose                    = false
  Logger logger                      = null

  String defaultProxyHost                 = null
  String defaultProxyPort                 = null
  
  // SSH command execution options.
  ExecOptions execOptions            = new ExecOptions()

  def execOptions(@DelegatesTo(strategy = DELEGATE_FIRST, value = ExecOptions) Closure cl) {
    cl.delegate = execOptions
    cl.resolveStrategy = DELEGATE_FIRST
    cl()
  }

  // SCP options.
  ScpOptions scpOptions            = new ScpOptions()

  def scpOptions(@DelegatesTo(strategy = DELEGATE_FIRST, value = ScpOptions) Closure cl) {
    cl.delegate = scpOptions
    cl.resolveStrategy = DELEGATE_FIRST
    cl()
  }

  static SysOutLogger systemOut() {
    new SysOutLogger()
  }

  static SysErrLogger systemErr() {
    new SysErrLogger()
  }

  static Slf4jLogger sf4j() {
    new Slf4jLogger()
  }

}
