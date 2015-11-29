/*
 * Copyright (C) 2011-2015 Aestas/IT
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

package com.aestasit.infrastructure.ssh.dsl

import com.aestasit.infrastructure.ssh.SshException
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.SftpATTRS

/**
 * This class represents a remote file and it gives some methods to access file's content.
 *
 * @author Andrey Adamovich
 * @author Luciano Fiandesio
 *
 */
class RemoteFile {

  private final SessionDelegate delegate
  private final String destination

  RemoteFile(SessionDelegate delegate, String destination) {
    this.delegate = delegate
    if (!destination || !destination.trim()) {
      throw new SshException("Remote file destination is not set!")
    }
    this.destination = destination
  }

  String getText() {
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

  void setText(String text) {
    File tempFile = File.createTempFile(this.getClass().getPackage().name, "txt")
    text.eachLine { line -> 
      tempFile << "${line.trim()}\n" 
    }
    try {
      delegate.scp {
        from { localFile(tempFile) }
        into { remoteFile(destination) }
      }
    } finally {
      tempFile.delete()
    } 
  }

  void touch() {
    delegate.exec(command: 'touch ' + this.destination, failOnError: true, showOutput: true)
  }

  String getOwner() {
    int uid = -1
    delegate.sftpChannel { ChannelSftp channel ->
      SftpATTRS attr = channel.stat( this.destination)
      uid =  attr.getUId()
    }
    delegate.exec("getent passwd ${uid} | cut -d: -f1").output.trim()
  }

  void setOwner(String user) {
    def uid = getUid(user)
    if (uid) {
      delegate.sftpChannel { ChannelSftp channel ->
        channel.chown(uid, this.destination)
      }
    } else {
      // TODO how do we handle a custom exception?
      // throw new NullPointerException('invalid user')
    }
  }

  String getGroup() {
    int uid = -1
    delegate.sftpChannel { ChannelSftp channel ->
      SftpATTRS attr = channel.stat( this.destination)
      uid =  attr.getGId()
    }
    delegate.exec("getent group ${uid} | cut -d: -f1").output.trim()
  }

  void setGroup(String group) {
    def gid = getGid(group)
    if (gid) {
      delegate.sftpChannel { ChannelSftp channel ->
        channel.chgrp(gid, this.destination)
      }
    } else {
      // TODO how do we handle a custom exception?
      // throw new NullPointerException('invalid user')
    }
  }

  void setPermissions(int mask) {
    delegate.sftpChannel { ChannelSftp channel ->
      // Convert the mask from octal to decimal
      // ChannelSftp requires decimal format
      channel.chmod(Integer.parseInt(mask.toString(),8), this.destination)
    }
  }

  int getPermissions() {
    int mask = 0
    delegate.sftpChannel { ChannelSftp channel ->
      SftpATTRS attr = channel.stat( this.destination)
      // Convert back to octal.
      mask = Integer.toOctalString(attr.getPermissions()).toInteger() - 100000
    }
    mask
  }

  /**
   * Get the uid of a user.
   * 
   * @param user user
   * @return an Integer representing the uid
   * or null if the user is not found
   */
  private Integer getUid(String user) {
    resolveId(delegate.exec("id -u ${user}"))
  }
  
  /**
   * Get the gid of a group.
   * 
   * @param group group name
   * @return an Integer representing the gid
   * or null if the group is not found
   */
  private Integer getGid(String group) {
    resolveId(delegate.exec("getent group ${group} | cut -d: -f3"))
  }

  /**
   * Tests that the path exists and is not a directory.
   * @return isNormalFile
   */
  boolean isFile() {
    !delegate.exec("test -f ${destination}").failed()
  }

  /**
   * Tests that the path exists and is not a file.
   * @return isNormalDirectory
   */
  boolean isDirectory() {
    !delegate.exec("test -d ${destination}").failed()
  }

  static private Integer resolveId(out) {
    if (out.output.isInteger()) {
      return  out.output.toInteger()
    }
    null
  }

}
