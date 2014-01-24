/*
 * Copyright (C) 2011-2013 Aestas/IT
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

package com.aestasit.ssh.dsl

import static com.aestasit.ssh.dsl.FileSetType.*
import static org.apache.commons.io.FilenameUtils.*
import static org.apache.commons.codec.digest.DigestUtils.*

import com.aestasit.ssh.ScpOptions
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
      ScpOptionsDelegate copySpec = new ScpOptionsDelegate()
      copySpec.with {
        from { localFile(sourceFile) }
        into { remoteDir(dst) }  
      }
      upload(copySpec, channel)
    }
  }

  def scp(Closure cl) {
    ScpOptionsDelegate copySpec = new ScpOptionsDelegate()
    cl.delegate = copySpec
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
    validateCopySpec(copySpec)
    sftpChannel { ChannelSftp channel ->
      if (copySpec.source.type == LOCAL) {
        upload(copySpec, channel)
      } else if (copySpec.source.type == REMOTE) {
        download(copySpec, channel)
      }
    }
  }

  private void validateCopySpec(ScpOptionsDelegate options) {
    if (options.source.type == null || options.source.type == UNKNOWN ||
    options.target.type == null || options.target.type == UNKNOWN) {
      throw new SshException("Either scp source (from) or target (into) is of unkown type!")
    }
    if (options.source.type == options.target.type) {
      throw new SshException("Scp source (from) and target (into) shouldn't be both local or both remote!")
    }
  }

  private void upload(ScpOptionsDelegate copySpec, ChannelSftp channel) {

    def remoteDirs = copySpec.target.remoteDirs
    def remoteFiles = copySpec.target.remoteFiles
    def scpOptions = new ScpOptions(options.scpOptions, copySpec)
    
    // Check if upload should go through an intermediate directory and append its path to all target paths.
    def uploadMap = [:]
    if (scpOptions.uploadToDirectory) {
      logger.debug("Uploading through: ${scpOptions.uploadToDirectory}")
      def uploadDirectory = separatorsToUnix(normalize(scpOptions.uploadToDirectory))
      createRemoteDirectory(uploadDirectory, channel)
      remoteDirs = remoteDirs.collect { String path ->
        def uploadPath = uploadDirectory + '/' + md5Hex(path)
        uploadMap[uploadPath] = path
        uploadPath
      }
      if (remoteFiles.size() > 0) {
        throw new SshException("Coping directly to remote file is not supported when uploading through a directiry!")
      }
    }
    
    // Create remote directories.
    remoteFiles.each { String dstFile ->
      def dstDir = getPath(dstFile)
      createRemoteDirectory(dstDir, channel)
    }
    remoteDirs.each { String dstFile ->
      createRemoteDirectory(dstFile, channel)
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
        def actualPath = '/' + relativePath(scpOptions.uploadToDirectory, copiedPath)
        exec {
          command = scpOptions.postUploadCommand.replaceAll('%from%', copiedPath).replaceAll('%to%', uploadMap[copiedPath])
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

  private String relativePath(File parent, File child) {
    separatorsToUnix(child.canonicalPath.replace(parent.canonicalPath, '')).replaceAll('^/', '')
  }

  private String relativePath(String parent, String child) {
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
    Vector<LsEntry> entries = channel.ls(separatorsToUnix(remoteDir))
    entries.each { LsEntry entry ->
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
    srcFile.withInputStream { input ->
      channel.put(input, dst, monitor)
    }
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
    new LoggerProgressMonitor(logger)
  }

}
