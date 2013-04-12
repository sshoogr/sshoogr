package com.aestasit.ssh.mocks

import org.apache.sshd.server.FileSystemView
import org.apache.sshd.server.SshFile

class MockFileSystemView implements FileSystemView {

  public SshFile getFile(String file) {
    return new MockSshFile(file)
  }

  public SshFile getFile(SshFile baseDir, String file) {
    return new MockSshFile(baseDir, file)
  }
}
