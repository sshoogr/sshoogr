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

package com.aestasit.infrastructure.ssh

import org.junit.Test

class RemoteFileTest extends BaseIntegrationTest {

  @Test
  void testFileTouch() {
    engine.remoteSession {
      exec('rm -rf /tmp/test.file')
      remoteFile('/tmp/test.file').touch()
      assert exec('ls -ltr /tmp/test.file').exitStatus == 0
    }
  }
  
  @Test
  void testSetOwner() {
    engine.remoteSession {
      exec('rm -rf /tmp/test.file')
      remoteFile('/tmp/test.file').touch()
      remoteFile('/tmp/test.file').setOwner('vagrant')
      assert remoteFile('/tmp/test.file').owner == 'vagrant'
    }
  }

  @Test
  void testSetGroup() {
    engine.remoteSession {
      exec('rm -rf /tmp/test3.file')
      remoteFile('/tmp/test3.file').touch()
      remoteFile('/tmp/test3.file').setGroup('lpadmin')
      assert remoteFile('/tmp/test3.file').group == 'lpadmin'
    }
  }
  
  @Test
  void testSetPermissions() {
    engine.remoteSession {
      exec('rm -rf /tmp/test2.file')
      remoteFile('/tmp/test2.file').touch()
      remoteFile('/tmp/test2.file').setPermissions(777)
      assert remoteFile('/tmp/test2.file').permissions == 777
    }
  }
}
