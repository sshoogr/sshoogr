/*
 * Copyright (C) 2011-2014 Aestas/IT
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

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.SftpATTRS
/**
 * This class represents a remote file and it gives some methods to access file's content.
 *
 * @author Andrey Adamovich
 *
 */
class RemoteFile {

  private final SessionDelegate delegate
  private final String destination

  RemoteFile(SessionDelegate delegate, String destination) {
    this.delegate = delegate
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
    int uid
    delegate.sftpChannel { ChannelSftp channel ->
      SftpATTRS attr = channel.stat( this.destination)
      uid =  attr.getUId()
    }

    delegate.exec("getent passwd ${uid} | cut -d: -f1").output.trim()
  }

  void setOwner(String user) {
    def out = delegate.exec ('id -u ' + user)
    def uid  = out.output.toInteger()
    delegate.sftpChannel { ChannelSftp channel ->
      channel.chown(uid, this.destination)
    }
  }

  String getGroup() {

  }

  void setGroup(String group) {

  }

  void setOwnerAndGroup(String user, String group) {

  }

  void setPermissions(int mask) {

    delegate.sftpChannel { ChannelSftp channel ->
      // Convert the mask from octal to decimal
      // ChannelSftp requires decimal format
      channel.chmod(Integer.parseInt(mask.toString(),8), this.destination)
    }
  }

  int getPermissions() {
    int mask
    delegate.sftpChannel { ChannelSftp channel ->
      SftpATTRS attr = channel.stat( this.destination)
      // Convert back to octal
      mask = Integer.toOctalString(attr.getPermissions()).toInteger()-10000
    }
    mask
  }

}
