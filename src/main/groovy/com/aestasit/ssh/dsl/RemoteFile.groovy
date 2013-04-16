package com.aestasit.ssh.dsl

/**
 * This class represents a remote file and it gives some methods to access file's content.
 *
 * @author Andrey Adamovich
 *
 */
class RemoteFile {

  private SessionDelegate delegate
  private String destination

  RemoteFile(SessionDelegate delegate, String destination) {
    this.delegate = delegate
    this.destination = destination
  }

  def String getText() {
    File tempFile = File.createTempFile(this.getClass().getPackage().name, "txt")
    try {
      delegate.scp {
        from { remoteFile destination }
        into { localFile tempFile }
      }
      return tempFile.text
    } finally {
      tempFile.delete()
    }
  }

  def void setText(String text) {
    File tempFile = File.createTempFile(this.getClass().getPackage().name, "txt")
    text.eachLine { line -> tempFile << "${line.trim()}\n" }
    try {
      delegate.scp(tempFile, destination)
    } finally {
      tempFile.delete()
    }
  }
}
