package com.aestasit.ssh.dsl

import org.apache.commons.io.output.TeeOutputStream

import com.aestasit.ssh.ExecOptions
import com.aestasit.ssh.SshException
import com.aestasit.ssh.log.LoggerOutputStream
import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSchException

/**
 * Mix-in class for SessionDelegate implementing EXEC functionality.
 *
 * @author Andrey Adamovich
 *
 */
class ExecMethods {

  def CommandOutput exec(String cmd) {
    doExec(cmd, new ExecOptions(options.execOptions))
  }

  def CommandOutput exec(Collection cmds) {
    doExec(cmds, new ExecOptions(options.execOptions))
  }

  def CommandOutput exec(Closure cl) {
    cl.delegate = new ExecOptionsDelegate()
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
    if (!cl.delegate.command) {
      new SshException('Remote command is not specified!')
    }
    doExec(cl.delegate.command, new ExecOptions(options.execOptions, cl.delegate.execOptions))
  }

  def CommandOutput exec(Map execOptions) {
    doExec(execOptions.command, new ExecOptions(options.execOptions, execOptions))
  }

  def prefix(String prefix, Closure cl) {
    def originalPrefix = options.execOptions.prefix
    options.execOptions.prefix = prefix
    cl.delegate = this
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
    options.execOptions.prefix = originalPrefix
  }

  def suffix(String prefix, Closure cl) {
    def originalSuffix = options.execOptions.suffix
    options.execOptions.prefix = suffix
    cl.delegate = this
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
    options.execOptions.suffix = originalSuffix
  }

  private CommandOutput doExec(Collection cmds, ExecOptions execOptions) {
    CommandOutput commandOutput = null
    cmds.each { cmd ->
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
    if (options.prefix) {
      cmd = "${options.prefix} ${cmd}"
    }
    if (options.suffix) {
      cmd = "${cmd} ${options.suffix}"
    }
    if (options.showCommand) {
      logger.info("> " + cmd)
    }
    ChannelExec channel = (ChannelExec) session.openChannel("exec")
    def savedOutput = new ByteArrayOutputStream()
    def output = savedOutput
    if (options.showOutput) {
      def systemOutput = new LoggerOutputStream(logger)
      output = new TeeOutputStream(savedOutput, systemOutput)
    }
    channel.command = cmd
    channel.outputStream = output
    channel.extOutputStream = output
    channel.setPty(true)
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

  private CommandOutput verifyExitCode(int exitCode, ExecOptions options) {
    if (exitCode != 0) {
      String msg = "Remote command failed with exit status $exitCode"
      if (options.failOnError) {
        throw new SshException(msg)
      } else {
        logger.warn(msg)
      }
    }
  }
}
