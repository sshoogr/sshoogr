/*
 * Copyright (C) 2011-2016 Aestas/IT
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
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

import static com.aestasit.infrastructure.ssh.dsl.ParsingUtils.resolveId

/**
 * This class represents a remote file and it gives some methods to access file's content.
 *
 * @author Andrey Adamovich
 * @author Luciano Fiandesio
 *
 */
@TypeChecked
@CompileStatic
class RemoteFile implements Appendable, Writable {

  public static final int EMPTY_ID = -1

  protected final SessionDelegate delegate
  protected final String destination

  /**
   * Remote file reference constructor.
   *
   * @param delegate session delegate to use for accessing remote file.
   * @param destination path to remote file.
   */
  RemoteFile(SessionDelegate delegate, String destination) {
    this.delegate = delegate
    this.destination = destination
    if (!destination || !destination.trim()) {
      throw new SshException('Remote file destination is not set!')
    }
  }

  /***
   *    __ _ _
   *   / _(_) |
   *  | |_ _| | ___   _ __  _ __ ___  _ __  ___
   *  |  _| | |/ _ \ | '_ \| '__/ _ \| '_ \/ __|
   *  | | | | |  __/ | |_) | | | (_) | |_) \__ \
   *  |_| |_|_|\___| | .__/|_|  \___/| .__/|___/
   *                 | |             | |
   *                 |_|             |_|
   */

  /**
   * Tests that the path exists and is not a directory.
   *
   * @return isNormalFile
   */
  boolean isFile() {
    !delegate.exec("test -f ${destination}").failed()
  }

  /**
   * Tests that the path exists and is not a file.
   *
   * @return isNormalDirectory
   */
  boolean isDirectory() {
    !delegate.exec("test -d ${destination}").failed()
  }

  /**
   * Returns file's owner.
   *
   * @return owner's user name.
   */
  String getOwner() {
    int uid = EMPTY_ID
    delegate.sftpChannel { ChannelSftp channel ->
      SftpATTRS attr = channel.stat(this.destination)
      uid = attr.UId
    }
    delegate.exec("getent passwd ${uid} | cut -d: -f1").output.trim()
  }

  /**
   * Sets remote file owner.
   *
   * @param user owner's user name.
   */
  void setOwner(String user) {
    def uid = getUid(user)
    if (uid) {
      delegate.sftpChannel { ChannelSftp channel ->
        channel.chown(uid, this.destination)
      }
    } else {
      delegate.logger.warn("User ${user} does not exist!")
    }
  }

  /**
   * Retrieves remote file owner group.
   *
   * @return owner group name.
   */
  String getGroup() {
    int uid = EMPTY_ID
    delegate.sftpChannel { ChannelSftp channel ->
      SftpATTRS attr = channel.stat(this.destination)
      uid = attr.GId
    }
    delegate.exec("getent group ${uid} | cut -d: -f1").output.trim()
  }

  /**
   * Sets remote file's owner group.
   *
   * @param group owner group name.
   */
  void setGroup(String group) {
    def gid = getGid(group)
    if (gid) {
      delegate.sftpChannel { ChannelSftp channel ->
        channel.chgrp(gid, this.destination)
      }
    } else {
      delegate.logger.warn("Group ${group} does not exist!")
    }
  }

  /**
   * Sets remote file's permission mask e.g. remoteFile('/home/user/for_all.txt').permissions = 0777
   *
   * @param mask permissions to set.
   */
  void setPermissions(int mask) {
    delegate.sftpChannel { ChannelSftp channel ->
      channel.chmod(mask, this.destination)
    }
  }

  /**
   * Retrieves remote file's permission mask.
   *
   * @return permission mask as decimal integer.
   */
  int getPermissions() {
    int mask = 0
    delegate.sftpChannel { ChannelSftp channel ->
      SftpATTRS attr = channel.stat(this.destination)
      mask = attr.permissions - 32768
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
   * @return an Integer representing the gid or null if the group is not found.
   */
  private Integer getGid(String group) {
    resolveId(delegate.exec("getent group ${group} | cut -d: -f3"))
  }

  /***
   *    __ _ _
   *   / _(_) |
   *  | |_ _| | ___    ___  _ __  ___
   *  |  _| | |/ _ \  / _ \| '_ \/ __|
   *  | | | | |  __/ | (_) | |_) \__ \
   *  |_| |_|_|\___|  \___/| .__/|___/
   *                          | |
   *                          |_|
   */

  /**
   * "Touches" remote file.
   */
  void touch() {
    delegate.exec(command: 'touch ' + this.destination, failOnError: true, showOutput: true)
  }


  /***
   *    __ _ _                        _             _
   *   / _(_) |                      | |           | |
   *  | |_ _| | ___    ___ ___  _ __ | |_ ___ _ __ | |_
   *  |  _| | |/ _ \  / __/ _ \| '_ \| __/ _ \ '_ \| __|
   *  | | | | |  __/ | (_| (_) | | | | ||  __/ | | | |_
   *  |_| |_|_|\___|  \___\___/|_| |_|\__\___|_| |_|\__|
   *
   */

  /**
   * Retrieves remote file content.
   *
   * @return file content.
   */
  String getText() {
    File tempFile = createTempFile()
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

  /**
   * Sets remote file content as text.
   *
   * @param text content to set.
   */
  void setText(String text) {
    File tempFile = createTempFile()
    tempFile.withWriter { writer ->
      text.readLines().each { String line ->
        writer.append("${line.trim()}\n")
      }
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

  @SuppressWarnings('FactoryMethodName')
  private File createTempFile() {
    File.createTempFile(this.getClass().package.name, 'txt')
  }

  /**
   * {@inheritDoc}
   */
  Appendable append(CharSequence csq) throws IOException {
    String originalText = this.text
    this.text = originalText + csq
    this
  }

  /**
   * {@inheritDoc}
   */
  Appendable append(CharSequence csq, int start, int end) throws IOException {
    append(csq.subSequence(start, end))
  }

  /**
   * {@inheritDoc}
   */
  Appendable append(char c) throws IOException {
    append(c.toString())
  }

  /**
   * Append character sequence in the end of the remote file.
   *
   * @param value character sequence
   * @return this object for chaining calls.
   */
  Appendable leftShift(CharSequence value) {
    append(value)
  }

  /**
   * Append local file contents in the end of the remote file.
   *
   * @param file local file reference.
   * @return this object for chaining calls.
   */
  Appendable leftShift(File file) {
    append(file.text)
  }

  /**
   * {@inheritDoc}
   */
  Writer writeTo(Writer out) throws IOException {
    out.append(this.text)
    out
  }

}
