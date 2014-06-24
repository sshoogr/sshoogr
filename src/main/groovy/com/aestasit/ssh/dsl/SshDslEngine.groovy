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

package com.aestasit.ssh.dsl

import static groovy.lang.Closure.DELEGATE_FIRST

import com.aestasit.ssh.SshOptions
import com.jcraft.jsch.JSch

/**
 * SSH DSL entry-point class that gives access to SessionDelegate instance.
 *
 * @author Andrey Adamovich
 *
 */
class SshDslEngine {

  private final JSch jsch
  private final Properties config
  private final SshOptions options
  private SessionDelegate delegate

  SshDslEngine(SshOptions options)  {
    this.options = options
    this.jsch = new JSch()
    this.config = new Properties()
    config.put("StrictHostKeyChecking", "no")
    config.put("HashKnownHosts",  "yes")
    config.put("PreferredAuthentications", "publickey,keyboard-interactive,password")
    jsch.config = config
  }

  def remoteSession(@DelegatesTo(strategy = DELEGATE_FIRST, value = SessionDelegate) Closure cl) {
    executeSession(cl, null, null)
  }

  def remoteSession(String url, @DelegatesTo(strategy = DELEGATE_FIRST, value = SessionDelegate) Closure cl) {
    remoteSession(url, null, cl)
  }

  def remoteSession(String url, Map context, @DelegatesTo(strategy = DELEGATE_FIRST, value = SessionDelegate) Closure cl) {
    executeSession(cl, context) { SessionDelegate sessionDelegate ->
      sessionDelegate.url = url
    }
  }

  private executeSession(@DelegatesTo(strategy = DELEGATE_FIRST, value = SessionDelegate) Closure cl, Object context, @DelegatesTo(strategy = DELEGATE_FIRST, value = SessionDelegate) Closure configure) {
    def result = null
    if (cl) {
      if (!options.reuseConnection || delegate == null) {
        delegate = new SessionDelegate(jsch, options)
      }
      if (configure != null) {
        configure(delegate)
      }
      cl.delegate = delegate
      cl.resolveStrategy = Closure.DELEGATE_FIRST
      result = cl(context)
      if ((!options.reuseConnection) &&
          delegate.session?.connected) {
        try {
          delegate.session.disconnect()
        } catch (Exception e) {
        }
      }
    }
    result
  }
}

