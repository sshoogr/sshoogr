package com.aestasit.ssh.mocks

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

import org.apache.sshd.server.Environment
import org.apache.sshd.server.ExitCallback

public class MockCommand implements org.apache.sshd.server.Command {

  private ExitCallback callback
  private OutputStream err
  private OutputStream out
  private InputStream inp
  private final String command

  def MockCommand() {
    super()
  }

  def MockCommand(String command) {
    super()
    this.command = command
  }

  def void setInputStream(InputStream inp) {
    this.inp = inp
  }

  def void setOutputStream(OutputStream out) {
    this.out = out
  }

  def void setErrorStream(OutputStream err) {
    this.err = err
  }

  def void setExitCallback(ExitCallback callback) {
    this.callback = callback
  }

  def void start(Environment env) throws IOException {
    if (command.startsWith("ls")) {
      out << '''total 20
drwxr-xr-x 3 1100 1100 4096 Aug  7 16:52 .
drwxr-xr-x 8 1100 1100 4096 Aug  1 17:53 ..
drwxr-xr-x 3 1100 1100 4096 Aug  7 16:49 examples
'''
      callback.onExit(0)
    } else if (command.startsWith("whoami")) {
      out << "root\n"
      callback.onExit(0)
    } else if (command.startsWith("du")) {
      out << "100\n"
      callback.onExit(0)
    } else if (command.startsWith("timeout")) {
      Thread.sleep(2000)
    } else if (command.startsWith("rm")) {
      out << "/tmp/test.file\n"
      callback.onExit(0)
    } else {    
      callback.onExit(1, "Unknown command: $command")
    }
  }

  def void destroy() {
  }
}
