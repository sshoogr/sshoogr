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
}
