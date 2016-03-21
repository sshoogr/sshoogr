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

package com.aestasit.infrastructure.ssh

import com.jcraft.jsch.JSchException
import org.junit.Test

import static com.aestasit.infrastructure.ssh.DefaultSsh.*
import static org.junit.Assert.fail

/**
 * Test for static default SSH DSL implementation.
 *
 * @author Andrey Adamovich
 *
 */
class DefaultSshTest extends BaseSshTest {

  @Test
  void testStaticMethods() {
    trustUnknownHosts = true
    execOptions {
      maxWait = 30000
    }
    remoteSession('user2:654321@localhost:2233') {
      exec 'whoami'
      exec 'du -s'
      exec 'rm -rf /tmp/test.file'
      scp testFile, '/tmp/test.file'
      remoteFile('/etc/init.conf').text = 'content'
    }
  }

  @Test
  void testUnknownHosts() {
    trustUnknownHosts = false
    try {
      remoteSession('user2:654321@localhost:2233') {
        exec 'whoami'
      }
      fail('Should fail with host reject!')
    } catch (JSchException e) {
      assert e.message.contains('reject')
    }
  }

  @Test
  void testOptionsOverride() {
    trustUnknownHosts = true

    // Default behaviour should show the progress.
    String output = captureOutput {
      remoteSession('user2:654321@localhost:2233') {
        scp {
          from { localDir new File(currentDir, 'test-settings') }
          into { remoteDir '/tmp/puppet' }
        }
      }
    }
    assert output.contains('100%')

    // Test scp delegate override.
    output = captureOutput {
      remoteSession('user2:654321@localhost:2233') {
        scp {
          showProgress = false
          from { localDir new File(currentDir, 'test-settings') }
          into { remoteDir '/tmp/puppet' }
        }
      }
    }
    assert !output.contains('bytes transferred')

    // Test global override inside session closure.
    output = captureOutput {
      remoteSession('user2:654321@localhost:2233') {
        scpOptions {
          showProgress = false
        }
        scp {
          from { localDir new File(currentDir, 'test-settings') }
          into { remoteDir '/tmp/puppet' }
        }
      }
    }
    assert !output.contains('bytes transferred')

  }
}