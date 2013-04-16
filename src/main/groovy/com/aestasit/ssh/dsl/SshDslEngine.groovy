package com.aestasit.ssh.dsl

import com.aestasit.ssh.SshOptions
import com.jcraft.jsch.JSch

/**
 * SSH DSL entry-point class that gives access to SessionDelegate instance.
 *
 * @author Andrey Adamovich
 *
 */
class SshDslEngine {

  private JSch jsch
  private Properties config
  private SshOptions options
  private SessionDelegate delegate

  SshDslEngine(SshOptions options)  {
    this.options = options
    this.jsch = new JSch()
    this.config = new Properties()
    config.put("StrictHostKeyChecking", "no")
    config.put("HashKnownHosts",  "yes")
    jsch.config = config
  }

  def remoteSession(Closure cl) {
    executeSession(cl, null, null)
  }

  def remoteSession(String url, Closure cl) {
    remoteSession(url, null, cl)
  }

  def remoteSession(String url, Map context, Closure cl) {
    executeSession(cl, context) { SessionDelegate sessionDelegate ->
      sessionDelegate.url = url
    }
  }

  private void executeSession(Closure cl, Object context, Closure configure) {
    if (cl != null) {
      if (!options.reuseConnection || delegate == null) {
        delegate = new SessionDelegate(jsch, options)
      }
      if (configure != null) {
        configure(delegate)
      }
      cl.delegate = delegate
      cl.resolveStrategy = Closure.DELEGATE_FIRST
      cl(context)
      if ((!options.reuseConnection) &&
      (delegate.session != null) &&
      delegate.session.connected) {
        try {
          delegate.session.disconnect()
        } catch (Exception e) {
        }
      }
    }
  }
}

