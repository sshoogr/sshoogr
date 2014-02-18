/*
 * Copyright (C) 2011-2014 Aestas/IT
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
  File defaultKeyFile              = null
  String defaultPassPhrase         = null
  String defaultPassword           = null
  int defaultPort                  = 22
  boolean trustUnknownHosts        = false
  boolean reuseConnection          = false
  boolean verbose                  = false
  Logger logger                    = null
  
  // SSH command execution options.
  ExecOptions execOptions          = new ExecOptions()

  def execOptions(Closure cl) {
    cl.delegate = execOptions
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
  }

  // SCP options.
  ScpOptions scpOptions            = new ScpOptions()

  def scpOptions(Closure cl) {
    cl.delegate = scpOptions
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
  }

}
