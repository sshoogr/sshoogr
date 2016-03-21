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
import com.aestasit.infrastructure.ssh.log.AnsiSessionLogger
import com.aestasit.infrastructure.ssh.log.SessionLogger
import com.aestasit.infrastructure.ssh.log.Slf4JSessionLogger
import com.aestasit.infrastructure.ssh.log.SysErrSessionLogger
import com.aestasit.infrastructure.ssh.log.SysOutSessionLogger
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
@SuppressWarnings('MethodCount')
class DefaultSsh {

  static SshOptions options = new SshOptions()

  static {
    options.with {
      logger = new SysOutSessionLogger()
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
    options.execOptions
  }

  static ScpOptions getScpOptions() {
    options.scpOptions
  }

  static scpOptions(@DelegatesTo(strategy = DELEGATE_FIRST, value = ScpOptions) Closure cl) {
    options.scpOptions(cl)
  }

  static String getDefaultHost() {
    options.defaultHost
  }

  static File getDefaultKeyFile() {
    options.defaultKeyFile
  }

  static String getDefaultPassword() {
    options.defaultPassword
  }

  static boolean getVerbose() {
    options.verbose
  }

  static boolean getTrustUnknownHosts() {
    options.trustUnknownHosts
  }

  static boolean getReuseConnection() {
    options.reuseConnection
  }

  static SessionLogger getLogger() {
    options.logger
  }

  static Boolean getFailOnError() {
    options.failOnError
  }

  static String getDefaultUser() {
    options.defaultUser
  }

  static int getDefaultPort() {
    options.defaultPort
  }

  static boolean isReuseConnection() {
    options.reuseConnection
  }

  static boolean isVerbose() {
    options.verbose
  }
  
  static boolean isTrustUnknownHosts() {
    options.trustUnknownHosts
  }

  static void setVerbose(boolean verbose) {
    options.verbose = verbose
  }
  
  static void setTrustUnknownHosts(boolean flag) {
    options.trustUnknownHosts = flag
  }
  
  static void setScpOptions(ScpOptions opts) {
    options.scpOptions = opts
  }
  
  static void setReuseConnection(boolean flag) {
    options.reuseConnection = flag
  }
  
  static void setLogger(SessionLogger logger) {
    options.logger = logger
  }
  
  static void setFailOnError(Boolean flag) {
    options.failOnError = flag
  }
  
  static void setExecOptions(ExecOptions opts) {
    options.execOptions = opts
  }
  
  static void setDefaultUser(String user) {
    options.defaultUser = user
  }
  
  static void setDefaultPort(int port) {
    options.defaultPort = port
  }
  
  static void setDefaultPassword(String password) {
    options.defaultPassword = password
  }
  
  static void setDefaultKeyFile(File key) {
    options.defaultKeyFile = key
  }
  
  static void setDefaultHost(String host) {
    options.defaultHost = host
  }

  static AnsiSessionLogger ansi() {
    new AnsiSessionLogger()
  }

  static SysOutSessionLogger systemOut() {
    new SysOutSessionLogger()
  }

  static SysErrSessionLogger systemErr() {
    new SysErrSessionLogger()
  }

  static Slf4JSessionLogger sf4j() {
    new Slf4JSessionLogger()
  }

}
