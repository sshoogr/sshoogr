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

package com.aestasit.infrastructure.ssh.dsl

import com.aestasit.infrastructure.ssh.ExecOptions
import com.aestasit.infrastructure.ssh.ScpOptions
import com.aestasit.infrastructure.ssh.SshException
import com.aestasit.infrastructure.ssh.SshOptions
import com.aestasit.infrastructure.ssh.log.*
import com.jcraft.jsch.*
import org.apache.commons.io.output.TeeOutputStream

import java.util.regex.Pattern

import static com.aestasit.infrastructure.ssh.dsl.FileSetType.*
import static groovy.lang.Closure.DELEGATE_FIRST
import static org.apache.commons.codec.digest.DigestUtils.md5Hex
import static org.apache.commons.io.FilenameUtils.*

/**
 * Closure delegate that is used to collect all SSH options and give access to other DSL delegates.
 *
 * @author Andrey Adamovich
 *
 */
@SuppressWarnings('MethodCount')
class SessionDelegate {

  private static final int DEFAULT_SSH_PORT = 22
  private static final Pattern SSH_URL = ~/^(([^:@]+)(:([^@]+))?@)?([^:]+)(:(\d+))?$/

  private String host = null
  private int port = DEFAULT_SSH_PORT
  private String username = null
  private File keyFile = null
  private String passPhrase = null
  private String password = null
  private boolean changed = false

  String proxyHost = null
  String proxyPort = null

  private Session session = null
  private final JSch jsch
  private final SshOptions options

  protected SessionLogger logger = null

  SessionDelegate(JSch jsch, SshOptions options) {

    this.jsch = jsch
    this.options = options // TODO: we should clone this object to allow per session settings.
    this.host = options.defaultHost
    this.username = options.defaultUser
    this.port = options.defaultPort
    this.password = options.defaultPassword
    this.keyFile = options.defaultKeyFile
    this.passPhrase = options.defaultPassPhrase

    this.proxyHost = options.defaultProxyHost
    this.proxyPort = options.defaultProxyPort

    if (options.logger != null) {
      logger = options.logger
    } else {
      logger = new Slf4JSessionLogger()
    }
    if (options.sshDebug) {
      jsch.setLogger(new JschLogger(logger))
    }

  }

  void connect() {

    try {
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
          if (passPhrase) {
            jsch.addIdentity(keyFile.absolutePath, passPhrase)
          } else {
            jsch.addIdentity(keyFile.absolutePath)
          }
        }

        session.setPassword(password as String)

        if (this.proxyHost?.trim() && this.proxyPort?.trim()) {
          session.proxy = new ProxyHTTP(this.proxyHost, Integer.parseInt(this.proxyPort))
        }

        if (options.verbose) {
          logger.info(">>> Connecting to $host")
        }

        session.connect()
      }
    } finally {
      changed = false
    }
  }

  void disconnect() {
    if (session?.connected) {
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

  void reconnect() {
    disconnect()
    connect()
  }

  void setUrl(String url) {
    def matcher = SSH_URL.matcher(url)
    if (matcher.matches()) {
      setHost(matcher.group(5))
      if (matcher.group(7)) {
        setPort(matcher.group(7).toInteger())
      } else {
        setPort(DEFAULT_SSH_PORT)
      }
      setUser(matcher.group(2))
      setPassword(matcher.group(4))
    } else {
      throw new MalformedURLException("Unknown URL format: " + url)
    }
  }

  protected void setChanged(boolean changed) {
    this.changed = changed
  }

  void setHost(String host) {
    this.changed = changed || (this.host != host)
    this.host = host
  }

  void setUser(String user) {
    this.changed = changed || (this.username != user)
    this.username = user
  }

  void setPassword(String password) {
    this.changed = changed || (this.password != password)
    this.password = password
  }

  void setProxyHost(String proxyHost) {
    this.changed = changed || (this.proxyHost != proxyHost)
    this.proxyHost = proxyHost
  }

  void setProxyPort(String proxyPort) {
    this.changed = changed || (this.proxyPort != proxyPort)
    this.proxyPort = proxyPort
  }

  void setPort(int port) {
    this.changed = changed || (this.port != port)
    this.port = port
  }

  void setKeyFile(File keyFile) {
    this.changed = changed || (this.keyFile != keyFile)
    this.keyFile = keyFile
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////
  //   _____  _____ _____
  //  / ____|/ ____|  __ \
  // | (___ | |    | |__) |
  //  \___ \| |    |  ___/
  //  ____) | |____| |
  // |_____/ \_____|_|
  //
  ////////////////////////////////////////////////////////////////////////////////////////////////

  def scpOptions(@DelegatesTo(strategy = DELEGATE_FIRST, value = ScpOptions) Closure cl) {
    options.scpOptions(cl)
  }

  def scp(String sourceFile, String dst) {
    scp(new File(sourceFile), dst)
  }

  def scp(File sourceFile, String dst) {
    sftpChannel { ChannelSftp channel ->
      ScpOptionsDelegate copySpec = new ScpOptionsDelegate(options.scpOptions)
      copySpec.with {
        from { localFile(sourceFile) }
        into { remoteDir(dst) }
      }
      upload(copySpec, channel)
    }
  }

  def scp(@DelegatesTo(strategy = DELEGATE_FIRST, value = ScpOptionsDelegate) Closure cl) {
    ScpOptionsDelegate copySpec = new ScpOptionsDelegate(options.scpOptions)
    cl.delegate = copySpec
    cl.resolveStrategy = DELEGATE_FIRST
    cl()
    scp(copySpec)
  }

  def scp(ScpOptionsDelegate copySpec) {
    validateCopySpec(copySpec)
    sftpChannel { ChannelSftp channel ->
      if (copySpec.source.type == LOCAL) {
        upload(copySpec, channel)
      } else if (copySpec.source.type == REMOTE) {
        download(copySpec, channel)
      }
    }
  }

  static private void validateCopySpec(ScpOptionsDelegate options) {
    if (options.source.type == null || options.source.type == UNKNOWN ||
      options.target.type == null || options.target.type == UNKNOWN) {
      throw new SshException("Either scp source (from) or target (into) is of unknown type!")
    }
    if (options.source.type == options.target.type) {
      throw new SshException("Scp source (from) and target (into) shouldn't be both local or both remote!")
    }
  }

  private void upload(ScpOptionsDelegate copySpec, ChannelSftp channel) {

    def remoteDirs = copySpec.target.remoteDirs
    def remoteFiles = copySpec.target.remoteFiles
    def scpOptions = new ScpOptions(options.scpOptions, copySpec)

    // Check if upload should go through an intermediate directory and append its path hash to all target paths.
    Map<String, String> uploadMap = [:] as Map<String, String>
    if (scpOptions.uploadToDirectory) {
      logger.debug("Uploading through: ${scpOptions.uploadToDirectory}")
      def uploadDirectory = separatorsToUnix(normalize(scpOptions.uploadToDirectory))
      createRemoteDirectory(uploadDirectory, channel)
      remoteDirs = remoteDirs.collect { String dstPath ->
        def uploadPath = uploadDirectory + '/' + md5Hex(dstPath)
        uploadMap[uploadPath] = dstPath
        uploadPath
      }
      remoteFiles = remoteFiles.collect { String dstPath ->
        def dstDir = getFullPathNoEndSeparator(dstPath)
        def dstName = new File(dstPath).name
        def uploadPath = uploadDirectory + '/' + md5Hex(dstDir) + '/' + dstName
        uploadMap[uploadPath] = dstDir
        uploadPath
      }
    }

    // Create remote directories.
    remoteFiles.each { String dstFile ->
      def dstDir = getFullPathNoEndSeparator(dstFile)
      createRemoteDirectory(dstDir, channel)
    }
    remoteDirs.each { String dstDir ->
      createRemoteDirectory(dstDir, channel)
    }

    // Upload local files and directories.
    def allLocalFiles = copySpec.source.localFiles + copySpec.source.localDirs
    allLocalFiles.each { File sourcePath ->
      if (sourcePath.isDirectory()) {
        sourcePath.eachFileRecurse { File childPath ->
          def relativePath = relativePath(sourcePath, childPath)
          logger.debug("Working with relative path: $relativePath")
          remoteDirs.each { String dstDir ->
            if (childPath.isDirectory()) {
              def dstParentDir = separatorsToUnix(concat(dstDir, relativePath))
              createRemoteDirectory(dstParentDir, channel)
            } else {
              def dstPath = separatorsToUnix(concat(dstDir, relativePath))
              doPut(channel, childPath.canonicalFile, dstPath, scpOptions)
            }
          }
        }
      } else {
        remoteDirs.each { String dstDir ->
          def dstPath = separatorsToUnix(concat(dstDir, sourcePath.name))
          doPut(channel, sourcePath, dstPath, scpOptions)
        }
        remoteFiles.each { String dstFile ->
          doPut(channel, sourcePath, dstFile, scpOptions)
        }
      }
    }

    // Move files to their final destination using predefined command.
    if (scpOptions.uploadToDirectory && scpOptions.postUploadCommand) {
      remoteDirs.each { String copiedPath ->
        exec {
          command = scpOptions.postUploadCommand.replaceAll('%from%', copiedPath).replaceAll('%to%', uploadMap[copiedPath])
        }
      }
      remoteFiles.each { String copiedFilePath ->
        def copiedPath = getFullPathNoEndSeparator(copiedFilePath)
        exec {
          command = scpOptions.postUploadCommand.replaceAll('%from%', copiedPath).replaceAll('%to%', uploadMap[copiedFilePath])
        }
      }
    }

  }

  private void download(ScpOptionsDelegate copySpec, ChannelSftp channel) {

    // Download remote files.
    copySpec.source.remoteFiles.each { String srcFile ->
      copySpec.target.localDirs.each { File dstDir ->
        dstDir.mkdirs()
        doGet(channel, srcFile, new File(dstDir.canonicalPath, getName(srcFile)), new ScpOptions(options.scpOptions, copySpec))
      }
      copySpec.target.localFiles.each { File dstFile ->
        dstFile.parentFile.mkdirs()
        doGet(channel, srcFile, dstFile, new ScpOptions(options.scpOptions, copySpec))
      }
    }

    // Download remote directories.
    copySpec.source.remoteDirs.each { String srcDir ->
      remoteEachFileRecurse(srcDir, channel) { String srcFile ->
        copySpec.target.localDirs.each { File dstDir ->
          def dstFile = new File(dstDir.canonicalPath, relativePath(srcDir, srcFile))
          dstFile.parentFile.mkdirs()
          doGet(channel, srcFile, new File(dstDir.canonicalPath, relativePath(srcDir, srcFile)), new ScpOptions(options.scpOptions, copySpec))
        }
      }
      copySpec.target.localFiles.each { File dstFile ->
        logger.warn("Can't copy remote directory ($srcDir) to a local file (${dstFile.path})!")
      }
    }

  }

  static private String relativePath(File parent, File child) {
    separatorsToUnix(child.canonicalPath.replace(parent.canonicalPath, '')).replaceAll('^/', '')
  }

  static private String relativePath(String parent, String child) {
    normalizeNoEndSeparator(child)
      .replace(normalizeNoEndSeparator(parent) + File.separatorChar, '')
      .replace(File.separatorChar.toString(), '/')
  }

  private void createRemoteDirectory(String dstFile, ChannelSftp channel) {
    boolean dirExists = true
    try {
      channel.lstat(dstFile)
    } catch (SftpException e) {
      dirExists = false
    }
    if (!dirExists) {
      logger.debug("Creating remote directory: $dstFile")
      channel.mkdir(dstFile)
    }
  }

  private void remoteEachFileRecurse(String remoteDir, ChannelSftp channel, Closure cl) {
    if (options.verbose) {
      logger.info("> Getting file list from ${remoteDir} directory")
    }
    Vector<ChannelSftp.LsEntry> entries = channel.ls(separatorsToUnix(remoteDir))
    entries.each { ChannelSftp.LsEntry entry ->
      def childPath = separatorsToUnix(concat(remoteDir, entry.filename))
      if (entry.attrs.isDir()) {
        if (!(entry.filename in ['.', '..'])) {
          remoteEachFileRecurse(childPath, channel, cl)
        }
      } else if (entry.attrs.isLink()) {
        def linkPath = channel.readlink(childPath)
        if (options.verbose) {
          logger.info("> Skipping symlink: ${linkPath}")
        }
      } else {
        cl(childPath)
      }
    }
  }

  private void doPut(ChannelSftp channel, File srcFile, String dst, ScpOptions scpOptions) {
    if (options.verbose) {
      logger.info("> ${srcFile.canonicalPath} => ${dst}")
    }
    def monitor = scpOptions.showProgress ? newMonitor() : null
    channel.put(srcFile.canonicalPath, dst, monitor)
  }

  private void doGet(ChannelSftp channel, String srcFile, File dstFile, ScpOptions scpOptions) {
    if (options.verbose) {
      logger.info("> ${srcFile} => ${dstFile.canonicalPath}")
    }
    def monitor = scpOptions.showProgress ? newMonitor() : null
    dstFile.withOutputStream { output ->
      channel.get(srcFile, output, monitor)
    }
  }

  public void sftpChannel(Closure cl) {
    connect()
    ChannelSftp channel = (ChannelSftp) session.openChannel("sftp")
    channel.connect()
    try {
      cl(channel)
    } finally {
      channel.disconnect()
    }
  }

  private SftpProgressMonitor newMonitor() {
    new LoggerProgressMonitor(logger)
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

  RemoteFile remoteFile(String destination) {
    new RemoteFile(this, destination)
  }

  RemoteFile remoteDir(String destination) {
    new RemoteFile(this, destination)
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

  def su(String password, @DelegatesTo(strategy = DELEGATE_FIRST, value = SessionDelegate) Closure cl) {
    su("root", password, cl)
  }

  def su(String username, String password,
         @DelegatesTo(strategy = DELEGATE_FIRST, value = SessionDelegate) Closure cl) {
    exec {
      command = "su $username $password"
      failOnError = true
      showOutput = false
    }
    cl.delegate = this
    cl.resolveStrategy = DELEGATE_FIRST
    cl()
    exec {
      command = "exit"
      failOnError = true
      showOutput = false
    }
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////
  //   ________   ________ _____
  //  |  ____\ \ / /  ____/ ____|
  //  | |__   \ V /| |__ | |
  //  |  __|   > < |  __|| |
  //  | |____ / . \| |___| |____
  //  |______/_/ \_\______\_____|
  //
  ////////////////////////////////////////////////////////////////////////////////////////////////

  private static final int RETRY_DELAY = 1000

  def execOptions(@DelegatesTo(strategy = DELEGATE_FIRST, value = ExecOptions) Closure cl) {
    options.execOptions(cl)
  }

  CommandOutput exec(String cmd) {
    doExec(cmd, new ExecOptions(options.execOptions))
  }

  CommandOutput exec(GString cmd) {
    doExec(cmd?.toString(), new ExecOptions(options.execOptions))
  }

  CommandOutput exec(Collection<String> cmds) {
    doExec(cmds, new ExecOptions(options.execOptions))
  }

  CommandOutput exec(@DelegatesTo(strategy = DELEGATE_FIRST, value = ExecOptionsDelegate) Closure cl) {
    ExecOptionsDelegate delegate = new ExecOptionsDelegate(options.execOptions)
    cl.delegate = delegate
    cl.resolveStrategy = DELEGATE_FIRST
    cl()
    if (!delegate.command) {
      new SshException('Remote command is not specified!')
    }
    doExec(delegate.command, new ExecOptions(options.execOptions, delegate.execOptions))
  }

  CommandOutput exec(Map<?, ?> execOptions) {
    if (!execOptions?.command) {
      throw new SshException("The 'command' parameter is not specified!")
    }
    doExec(execOptions?.command?.toString(), new ExecOptions(options.execOptions, execOptions))
  }

  /**
   * Execute the specified command and returns a boolean to
   * signal if the command execution was successful.
   *
   * @param cmd a command to execute remotely
   * @return true , if command was successful
   */
  boolean ok(String cmd) {
    doExec(cmd, new ExecOptions(options.execOptions, [failOnError: false, showOutput: false, showCommand: false])).exitStatus == 0
  }

  /**
   * Execute the specified command and returns a boolean to
   * signal if the command execution was unsuccessful.
   *
   * @param cmd a command to execute remotely
   * @return true , if command was unsuccessful
   */
  boolean fail(String cmd) {
    !ok(cmd)
  }

  def prefix(String prefix, @DelegatesTo(strategy = DELEGATE_FIRST, value = SessionDelegate) Closure cl) {
    def originalPrefix = options.execOptions.prefix
    options.execOptions.prefix = prefix
    def result = null
    try {
      cl.delegate = this
      cl.resolveStrategy = DELEGATE_FIRST
      result = cl()
    } finally {
      options.execOptions.prefix = originalPrefix
    }
    result
  }

  def suffix(String suffix, @DelegatesTo(strategy = DELEGATE_FIRST, value = SessionDelegate) Closure cl) {
    def originalSuffix = options.execOptions.suffix
    options.execOptions.suffix = suffix
    def result = null
    try {
      cl.delegate = this
      cl.resolveStrategy = DELEGATE_FIRST
      result = cl()
    } finally {
      options.execOptions.suffix = originalSuffix
    }
    result
  }

  private CommandOutput doExec(Collection<String> commands, ExecOptions execOptions) {
    CommandOutput commandOutput = null
    commands.each { String cmd ->
      commandOutput = doExec(cmd, new ExecOptions(options.execOptions, execOptions))
    }
    commandOutput
  }

  private CommandOutput doExec(String cmd, ExecOptions options) {
    connect()
    catchExceptions(options) {
      awaitTermination(executeCommand(cmd, options), options)
    }
  }

  private ChannelData executeCommand(String cmd, ExecOptions options) {
    session.timeout = options.maxWait
    String actualCommand = cmd
    if (options.escapeCharacters) {
      if (options.escapeCharacters.contains('\\')) {
        actualCommand = actualCommand.replace('\\', '\\\\')
      }
      options.escapeCharacters.each { ch ->
        if (ch != '\\') {
          actualCommand = actualCommand.replace(ch.toString(), '\\' + ch)
        }
      }
    }
    if (options.prefix) {
      actualCommand = "${options.prefix} ${actualCommand}"
    }
    if (options.suffix) {
      actualCommand = "${actualCommand} ${options.suffix}"
    }
    if (options.showCommand) {
      logger.info("> " + actualCommand)
    }
    ChannelExec channel = (ChannelExec) session.openChannel("exec")
    def savedOutput = new ByteArrayOutputStream()
    def output = savedOutput
    if (options.showOutput) {
      def systemOutput = new LoggerOutputStream(logger)
      output = new TeeOutputStream(savedOutput, systemOutput)
    }
    channel.command = actualCommand
    channel.outputStream = output
    channel.extOutputStream = output
    channel.setPty(options.usePty)
    channel.connect()
    new ChannelData(channel: channel, output: savedOutput)
  }

  class ChannelData {
    ByteArrayOutputStream output
    Channel channel
  }

  private CommandOutput awaitTermination(ChannelData channelData, ExecOptions options) {
    Channel channel = channelData.channel
    try {
      def thread = null
      thread =
        new Thread() {
          void run() {
            while (!channel.isClosed()) {
              if (thread == null) {
                return
              }
              try {
                sleep(RETRY_DELAY)
              } catch (Exception e) {
                // ignored
              }
            }
          }
        }
      thread.start()
      thread.join(options.maxWait)
      if (thread.isAlive()) {
        thread = null
        return failWithTimeout(options)
      } else {
        int ec = channel.exitStatus
        verifyExitCode(ec, options)
        return new CommandOutput(ec, channelData.output.toString())
      }
    } finally {
      channel.disconnect()
    }
  }

  private CommandOutput catchExceptions(ExecOptions options, Closure cl) {
    try {
      return cl()
    } catch (JSchException e) {
      if (e.getMessage().indexOf("session is down") >= 0) {
        return failWithTimeout(options)
      } else {
        return failWithException(options, e)
      }
    }
  }

  private CommandOutput failWithTimeout(ExecOptions options) {
    setChanged(true)
    if (options.failOnError) {
      throw new SshException("Session timeout!")
    } else {
      logger.warn("Session timeout!")
      return new CommandOutput(-1, "Session timeout!")
    }
  }

  private CommandOutput failWithException(ExecOptions options, Throwable e) {
    if (options.failOnError) {
      throw new SshException("Command failed with exception", e)
    } else {
      logger.warn("Caught exception: " + e.getMessage())
      return new CommandOutput(-1, e.getMessage(), e)
    }
  }

  private void verifyExitCode(int exitCode, ExecOptions options) {
    if (exitCode != 0) {
      String msg = "Remote command failed with exit status $exitCode"
      if (options.failOnError) {
        throw new SshException(msg)
      } else {
        if (options.showOutput) {
          logger.warn(msg)
        }
      }
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

  def tunnel(int localPort, String remoteHost, int remotePort,
             @DelegatesTo(strategy = DELEGATE_FIRST, value = SessionDelegate) Closure cl) {
    connect()
    session.setPortForwardingL(localPort, remoteHost, remotePort)
    cl.delegate = this
    cl.resolveStrategy = DELEGATE_FIRST
    cl()
  }

  def tunnel(String remoteHost, int remotePort,
             @DelegatesTo(strategy = DELEGATE_FIRST, value = SessionDelegate) Closure cl) {
    connect()
    int localPort = findFreePort()
    session.setPortForwardingL(localPort, remoteHost, remotePort)
    cl.delegate = this
    cl.resolveStrategy = DELEGATE_FIRST
    cl(localPort)
  }

  static private int findFreePort() {
    ServerSocket server = new ServerSocket(0)
    try {
      int port = server.getLocalPort()
      return port
    } finally {
      server?.close()
    }
  }

  Session getSession() {
    session
  }
}
