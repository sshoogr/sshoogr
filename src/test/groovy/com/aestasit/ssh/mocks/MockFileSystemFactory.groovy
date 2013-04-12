package com.aestasit.ssh.mocks

import java.io.IOException

import org.apache.sshd.common.Session
import org.apache.sshd.server.FileSystemFactory
import org.apache.sshd.server.FileSystemView

class MockFileSystemFactory implements FileSystemFactory {

  public FileSystemView createFileSystemView(Session session) throws IOException {
    return new MockFileSystemView()
  }

}
