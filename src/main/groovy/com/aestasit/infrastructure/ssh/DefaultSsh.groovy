/*
 * Copyright (C) 2011-2016 Aestas/IT
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

import com.aestasit.infrastructure.ssh.dsl.SessionDelegate
import com.aestasit.infrastructure.ssh.dsl.SshDslEngine
import com.aestasit.infrastructure.ssh.log.Logger
import com.aestasit.infrastructure.ssh.log.SysOutLogger
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

import static groovy.lang.Closure.DELEGATE_FIRST

/**
 * Default "static" implementation of SSH DSL to be used inside plain Groovy scripts.
 * 
 * @author Andrey Adamovich
 *
 */
@CompileStatic
@TypeChecked
class DefaultSsh {

  static SshOptions options = new SshOptions()

  static {
    options.with {
      logger = new SysOutLogger()
      verbose = true
      execOptions.with {
        showOutput = true
        showCommand = true
      }
      scpOptions.with {
        showProgress = true
      }
    }
  }

  static remoteSession(@DelegatesTo(strategy = DELEGATE_FIRST, value = SessionDelegate) Closure cl) {
    new SshDslEngine(options).remoteSession(cl)
  }

  static remoteSession(String url, @DelegatesTo(strategy = DELEGATE_FIRST, value = SessionDelegate) Closure cl) {
    new SshDslEngine(options).remoteSession(url, cl)
  }

  static remoteSession(String url, Map context, @DelegatesTo(strategy = DELEGATE_FIRST, value = SessionDelegate) Closure cl) {
    new SshDslEngine(options).remoteSession(url, context, cl)
  }

  static execOptions(@DelegatesTo(strategy = DELEGATE_FIRST, value = ExecOptions) Closure cl) {
    options.execOptions(cl)
  }

  static ExecOptions getExecOptions() {
    options.getExecOptions()
  }

  static ScpOptions getScpOptions() {
    options.getScpOptions()
  }

  static scpOptions(@DelegatesTo(strategy = DELEGATE_FIRST, value = ScpOptions) Closure cl) {
    options.scpOptions(cl)
  }

  static String getDefaultHost() {
    options.getDefaultHost()
  }

  static File getDefaultKeyFile() {
    options.getDefaultKeyFile()
  }

  static String getDefaultPassword() {
    options.getDefaultPassword()
  }

  static boolean getVerbose() {
    options.getVerbose()
  }

  static boolean getTrustUnknownHosts() {
    options.getTrustUnknownHosts()
  }

  static boolean getReuseConnection() {
    options.getReuseConnection()
  }

  static Logger getLogger() {
    options.getLogger()
  }

  static Boolean getFailOnError() {
    options.getFailOnError()
  }

  static String getDefaultUser() {
    options.getDefaultUser()
  }

  static int getDefaultPort() {
    options.getDefaultPort()
  }

  static boolean isReuseConnection() {
    options.isReuseConnection()
  }

  static boolean isVerbose() {
    options.isVerbose()
  }
  
  static boolean isTrustUnknownHosts() {
    options.isTrustUnknownHosts()
  }

  static void setVerbose(boolean verbose) {
    options.setVerbose(verbose)
  }
  
  static void setTrustUnknownHosts(boolean flag) {
    options.setTrustUnknownHosts(flag)
  }
  
  static void setScpOptions(ScpOptions opts) {
    options.setScpOptions(opts)
  }
  
  static void setReuseConnection(boolean flag) {
    options.setReuseConnection(flag)
  }
  
  static void setLogger(Logger logger) {
    options.setLogger(logger)
  }
  
  static void setFailOnError(Boolean flag) {
    options.setFailOnError(flag)
  }
  
  static void setExecOptions(ExecOptions opts) {
    options.setExecOptions(opts)
  }
  
  static void setDefaultUser(String user) {
    options.setDefaultUser(user)
  }
  
  static void setDefaultPort(int port) {
    options.setDefaultPort(port)
  }
  
  static void setDefaultPassword(String password) {
    options.setDefaultPassword(password)
  }
  
  static void setDefaultKeyFile(File key) {
    options.setDefaultKeyFile(key)
  }
  
  static void setDefaultHost(String host) {
    options.setDefaultHost(host)
  }
    
}
