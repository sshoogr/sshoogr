package com.aestasit.ssh.mocks

import org.apache.sshd.server.Command
import org.apache.sshd.common.Factory


class MockShellFactory implements Factory<Command> {

  public Command create() {
    return new MockCommand()
  }
}