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

package com.aestasit.ssh

import com.aestasit.ssh.dsl.SshDslEngine
import org.junit.BeforeClass
import org.junit.Test

class RemoteFileTest {

  static SshOptions options
  static SshDslEngine engine

  @BeforeClass
  static void defineOptions() {
    options = new SshOptions()
    options.with {

      logger = systemOut()

      defaultHost = '127.0.0.1'
      defaultUser = 'vagrant'
      defaultPassword = 'vagrant'
      defaultPort = 2222

      reuseConnection = true
      trustUnknownHosts = true

      verbose = true

      execOptions.with {
        showOutput = true
        failOnError = false
        succeedOnExitStatus = 0
        maxWait = 30000
        usePty = true
      }
    }
    engine = new SshDslEngine(options)
  }
  
  @Test
  void testFileTouch() {
    engine.remoteSession {
      exec('rm /tmp/test.file')
      remoteFile('/tmp/test.file').touch()
      assert exec('ls -ltr /tmp/test.file').exitStatus == 0
    }
  }
  
  @Test
  void testSetOwner() {
    engine.remoteSession {
      exec('rm /tmp/test.file')
      remoteFile('/tmp/test.file').touch()
      remoteFile('/tmp/test.file').setOwner('vagrant')
      assert remoteFile('/tmp/test.file').owner == 'vagrant'
    }
  }

  @Test
  void testSetGroup() {
    engine.remoteSession {
      exec('rm /tmp/test3.file')
      remoteFile('/tmp/test3.file').touch()
      remoteFile('/tmp/test3.file').setGroup('lpadmin')
      assert remoteFile('/tmp/test3.file').group == 'lpadmin'
    }
  }
  
  @Test
  void testSetPermissions() {
    engine.remoteSession {
      exec('rm -fr /tmp/test2.file')
      remoteFile('/tmp/test2.file').touch()
      remoteFile('/tmp/test2.file').setPermissions(777)
      assert remoteFile('/tmp/test2.file').permissions == 777
    }
  }
}
