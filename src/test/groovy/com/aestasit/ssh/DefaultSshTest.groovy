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

package com.aestasit.ssh

import static com.aestasit.ssh.DefaultSsh.*

import org.junit.Test

/**
 * Test for static default SSH DSL implementation.
 *
 * @author Andrey Adamovich
 *
 */
class DefaultSshTest extends BaseSshTest {

  @Test
  def void testRemoteFile() throws Exception {
    remoteSession('user2:654321@localhost:2233') {
      exec 'whoami'
      exec 'du -s'
      exec 'rm -rf /tmp/test.file'
      scp testFile, '/tmp/test.file'
      remoteFile('/etc/init.conf').text = 'content'
    }
  }

}