package com.aestasit.ssh.dsl

import java.util.regex.Pattern

import com.aestasit.ssh.SshException
import com.aestasit.ssh.SshOptions
import com.aestasit.ssh.log.Logger
import com.aestasit.ssh.log.Slf4jLogger
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session

/**
 * Closure delegate that is used to collect all SSH options and give access to other DSL delegates.
 *
 * @author Andrey Adamovich
 *
 */
@Mixin([ScpMethods, ExecMethods])
class SessionDelegate {

  private static final int DEFAULT_SSH_PORT = 22
  private static final int RETRY_DELAY = 1000
  private static final Pattern SSH_URL = ~/^(([^:\@]+)(:([^\@]+))\@)?([^:]+)(:(\d+))?$/

  private String     host     = null
  private int        port     = DEFAULT_SSH_PORT
  private String     username = null
  private File       keyFile  = null
  private String     password = null
  private boolean    changed  = false

  private Session    session  = null
  private JSch       jsch     = null
  private SshOptions options  = null

  protected Logger logger     = null

  SessionDelegate(JSch jsch, SshOptions options) {
    this.jsch = jsch
    this.options = options
    this.host = options.defaultHost
    this.username = options.defaultUser
    this.port = options.defaultPort
    this.password = options.defaultPassword
    this.keyFile = options.defaultKeyFile
    if (options.logger != null) {
      logger = options.logger
    } else {
      logger = new Slf4jLogger()
    }
  }

  def connect() {
    try  {
      if (session == null || !session.connected || changed) {

        disconnect()

        if (host == null) {
          throw new SshException("Host is required.")
        }
        if (username == null) {
          throw new SshException("Username is required.")
        }
        if (keyFile == null && password == null) {
          throw new SshException("Password or key file is required.")
        }
        session = jsch.getSession(username, host, port)
        if (keyFile != null) {
          jsch.addIdentity(keyFile.absolutePath)
        }

        session.password = password

        if (options.verbose) {
          logger.info(">>> Connecting to $host")
        }

        session.connect()
      }
    } finally {
      changed = false
    }
  }

  def disconnect() {
    if ((session != null) && session.connected) {
      try {
        session.disconnect()
      } catch (Exception e) {
      } finally {
        if (options.verbose) {
          logger.info("<<< Disconnected from $host")
        }
      }
    }
  }

  def reconnect() {
    disconnect()
    connect()
  }

  def setUrl(String url) {
    def matcher = SSH_URL.matcher(url)
    if (matcher.matches()) {
      setHost(matcher.group(5))
      setPort(matcher.group(7).toInteger())
      setUser(matcher.group(2))
      setPassword(matcher.group(4))
    } else {
      throw new SshException("Unknown URL format: " + url)
    }
  }

  protected void setChanged(boolean changed) {
    this.changed = changed
  }

  public void setHost(String host) {
    this.changed = changed || (this.host == host)
    this.host = host
  }

  public void setUser(String user) {
    this.changed = changed || (this.username == user)
    this.username = user
  }

  public void setPassword(String password) {
    this.changed = changed || (this.password = password)
    this.password = password
  }

  public void setPort(int port) {
    this.changed = changed || (this.port = port)
    this.port = port
  }

  public void setKeyFile(File keyFile) {
    this.changed = changed || (this.keyFile = keyFile)
    this.keyFile = keyFile
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////
  //  ______ _____
  // |  ____/ ____|
  // | |__ | (___
  // |  __| \___ \
  // | |    ____) |
  // |_|   |_____/
  //
  ////////////////////////////////////////////////////////////////////////////////////////////////

  def RemoteFile remoteFile(String destination) {
    return new RemoteFile(this, destination)
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////
  //   _____ _    _
  //  / ____| |  | |
  // | (___ | |  | |
  //  \___ \| |  | |
  //  ____) | |__| |
  // |_____/ \____/
  //
  ////////////////////////////////////////////////////////////////////////////////////////////////

  def su(String password, Closure cl) {
    su("root", password, cl)
  }

  def su(String username, String password, Closure cl) {
    exec {
      command = "su $username $password"
      failOnError = true
      showOutput = false
    }
    cl.delegate = this
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
    exec {
      command = "exit"
      failOnError = true
      showOutput = false
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////
  //   _______ _    _ _   _ _   _ ______ _
  //  |__   __| |  | | \ | | \ | |  ____| |
  //     | |  | |  | |  \| |  \| | |__  | |
  //     | |  | |  | | . ` | . ` |  __| | |
  //     | |  | |__| | |\  | |\  | |____| |____
  //     |_|   \____/|_| \_|_| \_|______|______|
  //
  ////////////////////////////////////////////////////////////////////////////////////////////////

  def tunnel(int localPort, String remoteHost, int remotePort, Closure cl) {
    connect()
    session.setPortForwardingL(localPort, remoteHost, remotePort)
    cl.delegate = this
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
  }

  def tunnel(String remoteHost, int remotePort, Closure cl) {
    connect()
    int localPort = findFreePort()
    session.setPortForwardingL(localPort, remoteHost, remotePort)
    cl.delegate = this
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl(localPort)
  }

  private int findFreePort() {
    ServerSocket server = new ServerSocket(0)
    try {
      int port = server.getLocalPort()
      return port
    } finally {
      server?.close()
    }
  }

}
