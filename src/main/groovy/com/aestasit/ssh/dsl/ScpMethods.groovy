package com.aestasit.ssh.dsl;

import static com.aestasit.ssh.dsl.FileSetType.*
import static org.apache.commons.io.FilenameUtils.*

import org.omg.CORBA.UNKNOWN

import com.aestasit.ssh.SshException
import com.aestasit.ssh.log.LoggerProgressMonitor
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.SftpException
import com.jcraft.jsch.SftpProgressMonitor
import com.jcraft.jsch.ChannelSftp.LsEntry

/**
 * Mix-in class for SessionDelegate implementing SCP functionality.
 *
 * @author Andrey Adamovich
 *
 */
class ScpMethods {

  def scp(String sourceFile, String dst) {
    scp(new File(sourceFile), dst)
  }

  def scp(File sourceFile, String dst) {
    sftpChannel { ChannelSftp channel ->
      doPut(channel, sourceFile, dst)
    }
  }

  def scp(Closure cl) {
    ScpOptionsDelegate options = new ScpOptionsDelegate()
    cl.delegate = options
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
    validateOptions(options)
    sftpChannel { ChannelSftp channel ->
      if (options.source.type == LOCAL) {
        upload(options, channel)
      } else if (options.source.type == REMOTE) {
        download(options, channel)
      }
    }
  }

  private void validateOptions(ScpOptionsDelegate options) {
    if (options.source.type == null || options.source.type == UNKNOWN ||
    options.target.type == null || options.target.type == UNKNOWN) {
      throw new SshException("Either scp source (from) or target (into) is of unkown type!")
    }
    if (options.source.type == options.target.type) {
      throw new SshException("Scp source (from) and target (into) shouldn't be both local or both remote!")
    }
  }

  private void upload(ScpOptionsDelegate options, ChannelSftp channel) {

    // Create remote directories.
    options.target.remoteFiles.each { String dstFile ->
      def dstDir = getPath(dstFile)
      createRemoteDirectory(dstDir, channel)
    }
    options.target.remoteDirs.each { String dstFile ->
      createRemoteDirectory(dstFile, channel)
    }

    // Upload local files and directories.
    def allLocalFiles = options.source.localFiles + options.source.localDirs
    allLocalFiles.each { File sourcePath ->
      if (sourcePath.isDirectory()) {
        sourcePath.eachFileRecurse { File childPath ->
          def relativePath = relativePath(sourcePath, childPath)
          logger.debug("Working with relative path: $relativePath")
          options.target.remoteDirs.each { String dstDir ->
            if (childPath.isDirectory()) {
              def dstParentDir = separatorsToUnix(concat(dstDir, relativePath))
              createRemoteDirectory(dstParentDir, channel)
            } else {
              def dstPath = separatorsToUnix(concat(dstDir, relativePath))
              doPut(channel, childPath.canonicalFile, dstPath)
            }
          }
        }
      } else {
        options.target.remoteDirs.each { String dstDir ->
          def dstPath = separatorsToUnix(concat(dstDir, sourcePath.name))
          doPut(channel, sourcePath, dstPath)
        }
        options.target.remoteFiles.each { String dstFile ->
          doPut(channel, sourcePath, dstFile)
        }
      }
    }

  }

  private void download(ScpOptionsDelegate options, ChannelSftp channel) {

    // Download remote files.
    options.source.remoteFiles.each { String srcFile ->
      options.target.localDirs.each { File dstDir ->
        dstDir.mkdirs()
        doGet(channel, srcFile, new File(dstDir.canonicalPath, getName(srcFile)))
      }
      options.target.localFiles.each { File dstFile ->
        dstFile.parentFile.mkdirs()
        doGet(channel, srcFile, dstFile)
      }
    }

    // Download remote directories.
    options.source.remoteDirs.each { String srcDir ->
      remoteEachFileRecurse(srcDir, channel) { String srcFile ->
        options.target.localDirs.each { File dstDir ->
          def dstFile = new File(dstDir.canonicalPath, relativePath(srcDir, srcFile))
          dstFile.parentFile.mkdirs()
          doGet(channel, srcFile, new File(dstDir.canonicalPath, relativePath(srcDir, srcFile)))
        }
      }
      options.target.localFiles.each { File dstFile ->
        logger.warn("Can't copy remote directory ($srcDir) to a local file (${dstFile.path})!")
      }
    }

  }

  private String relativePath(File parent, File child) {
    separatorsToUnix(child.canonicalPath.replace(parent.canonicalPath, '')).replaceAll('^/', '')
  }

  private String relativePath(String parent, String child) {
    normalizeNoEndSeparator(child)
        .replace(normalizeNoEndSeparator(parent) + File.separatorChar, '')
        .replace(File.separatorChar.toString(), '/')
  }

  private void createRemoteDirectory(String dstFile, ChannelSftp channel) {
    logger.debug("Creating remote directory: $dstFile")
    boolean dirExists = true
    try {
      channel.lstat(dstFile)
    } catch (SftpException e) {
      dirExists = false
    }
    if (!dirExists) {
      channel.mkdir(dstFile)
    }
  }

  private void remoteEachFileRecurse(String remoteDir, ChannelSftp channel, Closure cl) {
    if (options.scpOptions.verbose) {
      logger.info("> Getting file list from ${remoteDir} directory")
    }
    Vector<LsEntry> entries = channel.ls(separatorsToUnix(remoteDir))
    entries.each { LsEntry entry ->
      def childPath = separatorsToUnix(concat(remoteDir, entry.filename))
      if (entry.attrs.isDir()) {
        if (!(entry.filename in ['.', '..'])) {
          remoteEachFileRecurse(childPath, channel, cl)
        }
      } else if (entry.attrs.isLink()) {
        def linkPath = channel.readlink(childPath)
        if (options.scpOptions.verbose) {
          logger.info("> Skipping symlink: ${linkPath}")
        }
      } else {
        cl(childPath)
      }
    }
  }

  private void doPut(ChannelSftp channel, File srcFile, String dst) {
    if (options.scpOptions.verbose) {
      logger.info("> ${srcFile.canonicalPath} => ${dst}")
    }
    def monitor = options.scpOptions.showProgress ? newMonitor() : null
    srcFile.withInputStream { input ->
      channel.put(input, dst, monitor)
    }
  }

  private void doGet(ChannelSftp channel, String srcFile, File dstFile) {
    if (options.scpOptions.verbose) {
      logger.info("> ${srcFile} => ${dstFile.canonicalPath}")
    }
    def monitor = options.scpOptions.showProgress ? newMonitor() : null
    dstFile.withOutputStream { output ->
      channel.get(srcFile, output, monitor)
    }
  }

  private void sftpChannel(Closure cl) {
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
    return new LoggerProgressMonitor(logger)
  }

}
