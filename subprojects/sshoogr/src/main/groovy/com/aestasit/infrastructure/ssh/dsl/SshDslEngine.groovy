/*
 * Copyright (C) 2011-2020 Aestas/IT
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
package com.aestasit.infrastructure.ssh.dsl

import com.aestasit.infrastructure.ssh.SshOptions
import com.jcraft.jsch.JSch
import groovy.transform.CompileStatic

import static groovy.lang.Closure.DELEGATE_FIRST

/**
 * SSH DSL entry-point class that gives access to SessionDelegate instance.
 *
 * @author Andrey Adamovich
 *
 */
@CompileStatic
@SuppressWarnings(['CatchThrowable', 'CatchException'])
class SshDslEngine {

  public static final String SSH_STRICT_HOST_KEY_CHECKING = 'StrictHostKeyChecking'
  public static final String SSH_HASH_KNOWN_HOSTS = 'HashKnownHosts'
  public static final String SSH_PREFERRED_AUTHENTICATIONS = 'PreferredAuthentications'
  public static final String DEFAULT_AUTH_METHODS = 'publickey,keyboard-interactive,password'

  private final JSch jsch
  private final Properties config
  private final SshOptions options

  private SessionDelegate delegate

  SshDslEngine(SshOptions options) {
    this.options = options
    this.jsch = new JSch()
    this.config = new Properties()
    config.put(SSH_STRICT_HOST_KEY_CHECKING, options.trustUnknownHosts ? 'no' : 'yes')
    if (!options.jschProperties.containsKey(SSH_HASH_KNOWN_HOSTS)) {
      config.put(SSH_HASH_KNOWN_HOSTS, 'yes')
    }
    if (!options.jschProperties.containsKey(SSH_PREFERRED_AUTHENTICATIONS)) {
      config.put(SSH_PREFERRED_AUTHENTICATIONS, DEFAULT_AUTH_METHODS)
    }
    config.putAll(options.jschProperties)
    jsch.config = config
  }

  def remoteSession(@DelegatesTo(strategy = DELEGATE_FIRST, value = SessionDelegate) Closure cl) {
    executeSession(cl, null, null)
  }

  def remoteSession(String url, @DelegatesTo(strategy = DELEGATE_FIRST, value = SessionDelegate) Closure cl) {
    remoteSession(url, null, cl)
  }

  def remoteSession(String url, Object context,
                     @DelegatesTo(strategy = DELEGATE_FIRST, value = SessionDelegate) Closure cl) {
    executeSession(cl, context) { SessionDelegate sessionDelegate ->
      sessionDelegate.url = url
    }
  }

  private executeSession(
    @DelegatesTo(strategy = DELEGATE_FIRST, value = SessionDelegate) Closure cl, Object context,
    @DelegatesTo(strategy = DELEGATE_FIRST, value = SessionDelegate) Closure configure) {
    def result = null
    if (cl) {
      if (!options.reuseConnection || delegate == null) {
        delegate = new SessionDelegate(jsch, options)
      }
      if (configure != null) {
        configure(delegate)
      }
      cl.delegate = delegate
      cl.resolveStrategy = DELEGATE_FIRST
      try {
        result = cl(context)
        if ((!options.reuseConnection) &&
          delegate.session?.connected) {
          safeDisconnect(delegate)
        }
      } catch (Throwable ex) {
        safeDisconnect(delegate)
        throw ex
      }
    }
    result
  }

  private static void safeDisconnect(SessionDelegate delegate) {
    try {
      delegate.disconnect()
    } catch (Exception e) {
      delegate.logger.warn(e.message)
    }
  }
}

