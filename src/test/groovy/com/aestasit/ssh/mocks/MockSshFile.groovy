package com.aestasit.ssh.mocks

import org.apache.sshd.server.SshFile

class MockSshFile implements SshFile {

  private final String file
  private final SshFile baseDir;

  private Map files = [
    '/tmp/puppet': [
      'isDirectory': true,
      'doesExist': false
      ],
    '.': [
      'isDirectory': true,
      'doesExist': true
      ],
    '/': [
      'isDirectory': true,
      'doesExist': true
      ],
    '/tmp': [
      'isDirectory': true,
      'doesExist': true
      ]
    ] 
  
  public MockSshFile(String file) {
    super()
    this.file = file
  }

  public MockSshFile(SshFile baseDir, String file) {
    super()
    this.baseDir = baseDir
    this.file = file
  }

  public String getAbsolutePath() {
    return file
  }

  public String getName() {
    return file
  }

  public String getOwner() {
    return "root"
  }

  public boolean isDirectory() {
    return files[file]['isDirectory']
  }

  public boolean isFile() {
    return !files[file]?.getAt('isDirectory')
  }

  public boolean doesExist() {
    return files[file]?.getAt('doesExist')
  }

  public boolean isReadable() {
    return true
  }

  public boolean isWritable() {
    return true
  }

  public boolean isExecutable() {
    return true
  }

  public boolean isRemovable() {
    return true
  }

  public SshFile getParentFile() {
    return baseDir
  }

  public long getLastModified() {
    return 0
  }

  public boolean setLastModified(long time) {
    return true
  }

  public long getSize() {
    return 0
  }

  public boolean mkdir() {
    return true
  }

  public boolean delete() {
    return true
  }

  public boolean create() throws IOException {
    return true
  }

  public void truncate() throws IOException {
  }

  public boolean move(SshFile destination) {
    return true
  }

  public List<SshFile> listSshFiles() {
    return new ArrayList<SshFile>()
  }

  public OutputStream createOutputStream(long offset) throws IOException {
    return new ByteArrayOutputStream()
  }

  public InputStream createInputStream(long offset) throws IOException {
    return new ByteArrayInputStream("data".getBytes("UTF-8"))
  }

  public void handleClose() throws IOException {
  }
  
}
