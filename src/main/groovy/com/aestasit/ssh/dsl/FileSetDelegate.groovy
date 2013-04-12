package com.aestasit.ssh.dsl

import static com.aestasit.ssh.dsl.FileSetType.*

import com.aestasit.ssh.SshException

/**
 * Closure delegate that is used to collect data about remote or local file collection.
 *
 * @author Andrey Adamovich
 *
 */
class FileSetDelegate {

  private FileSetType type = UNKNOWN
  private List<File> localDirs = new ArrayList<File>()
  private List<File> localFiles = new ArrayList<File>()
  private List<String> remoteFiles = new ArrayList<String>()
  private List<String> remoteDirs = new ArrayList<String>()

  def localFile(String file) {
    if (file) {
      setType(LOCAL)
      localFiles += new File(file)
    }
  }

  def localFile(File file) {
    if (file) {
      setType(LOCAL)
      localFiles += file
    }
  }

  def localDir(String dir) {
    if (dir) {
      setType(LOCAL)
      localDirs += new File(dir)
    }
  }

  def localDir(File dir) {
    if (dir) {
      setType(LOCAL)
      localDirs += dir
    }
  }

  def remoteFile(String file) {
    if (file) {
      setType(REMOTE)
      remoteFiles += file
    }
  }

  def remoteDir(String dir) {
    if (dir) {
      setType(REMOTE)
      remoteDirs += dir
    }
  }

  private setType(FileSetType type) {
    if (this.type == UNKNOWN || this.type == type) {
      this.type = type
    } else {
      throw new SshException("File set can not contain both local and remote source or target definitions!")
    }
  }
}
