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

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import com.aestasit.ssh.dsl.SshDslEngine
import com.aestasit.ssh.log.SysOutLogger

/**
 * SSH DSL test case that verifies different DSL syntax use cases.
 *
 * @author Andrey Adamovich
 *
 */
class SshDslTest extends BaseSshTest {

  static SshOptions options
  static SshDslEngine engine

  @BeforeClass
  def static void defineOptions() {
    options = new SshOptions()
    options.with {

      logger = new SysOutLogger()

      defaultHost = '127.0.0.1'
      defaultUser = 'user1'
      defaultPassword = '123456'
      defaultPort = 2233

      reuseConnection = true
      trustUnknownHosts = true

      verbose = true

      execOptions.with {
        showOutput = true
        failOnError = false
        succeedOnExitStatus = 0
        maxWait = 30000
        outputFile = new File("output.file")
        appendFile = true
        usePty = true
      }

    }
    engine = new SshDslEngine(options)
  }

  @Test
  void testDefaultSettings() throws Exception {
    engine.remoteSession {
      exec 'whoami'
      exec 'du -s'
      exec 'rm -rf /tmp/test.file'
      scp testFile, '/tmp/test.file'
    }
  }

  @Test
  void testUrlAndOverriding() throws Exception {
    // Test overriding default connection settings through URL.
    engine.remoteSession {

      url = 'user2:654321@localhost:2233'

      exec 'whoami'
      exec 'du -s'
      exec 'rm -rf /tmp/test.file'
      scp testFile, '/tmp/test.file'

    }
  }

  @Test
  void testPasswordlessLogin() throws Exception {
    engine.remoteSession {
      url = 'user2@localhost:2233'
      keyFile = getTestKey()
      exec 'whoami'
    }
  }

  @Test
  void testMethodOverriding() throws Exception {
    // Test overriding default connection settings through method parameter.
    engine.remoteSession('user2:654321@localhost:2233') {

      exec 'whoami'
      exec 'du -s'
      exec 'rm -rf /tmp/test.file'
      scp testFile, '/tmp/test.file'

    }
  }

  @Test
  void testPropertyOverriding() throws Exception {
    // Test overriding default connection settings through delegate parameters.
    engine.remoteSession {

      host = 'localhost'
      username = 'user2'
      password = '654321'
      port = 2233

      exec 'whoami'
      exec 'du -s'
      exec 'rm -rf /tmp/test.file'
      scp testFile, '/tmp/test.file'

    }
  }

  @Test
  void testOutputScripting() throws Exception {
    // Test saving the output and setting exec parameters through a builder.
    engine.remoteSession {
      println ">>>>> COMMAND: whoami"
      def output = exec(command: 'whoami', showOutput: false)
      output.output.eachLine { line -> println ">>>>> OUTPUT: ${line.reverse()}" }
      println ">>>>> EXIT: ${output.exitStatus}"
    }
  }

  @Test
  void testFailedStatus() {
    engine.remoteSession {
      assert exec('i should fail!').failed()
    }
  }

  @Test
  void testFailOnError() throws Exception {
    engine.remoteSession {
      exec(command: 'abcd', failOnError: false)
    }
  }

  @Test
  void testTimeout() throws Exception {
    try {
      engine.remoteSession {
        exec(command: 'timeout', maxWait: 1000)
      }
    } catch (SshException e) {
      assert e.cause.message.contains('timeout')
    }
  }

  @Test
  void testExecClosure() throws Exception {
    // Test closure based builder for exec.
    engine.remoteSession { exec { command = 'whoami' } }
  }

  @Test
  void testCopy() throws Exception {
    engine.remoteSession {
      scp {
        from {
          localDir new File(getCurrentDir(), 'test-settings')
        }
        into { remoteDir '/tmp/puppet' }
      }
    }
  }

  @Test
  void testSudoCopy() throws Exception {
    engine.remoteSession {
      scp {
        uploadToDirectory = '/tmp'
        from {
          localDir new File(getCurrentDir(), 'test-settings')
        }
        into { remoteDir '/etc/puppet' }
      }
    }
  }

  @Test
  void testMultiExec() throws Exception {
    engine.remoteSession {
      exec([
	  'ls -la',
	  'whoami'
      ])
      exec(failOnError: false, showOutput: true, command: [
	  'ls -la',
	  'whoami'
      ])
    }
  }


  @Test
  void testEscaping() throws Exception {
    engine.remoteSession {
      exec(command: 'ls -la "\\', escapeCharacters: '"\\')
      exec(command: 'ls -la "\\', escapeCharacters: ['"', '\\'])
      exec(command: 'ls -la "\\', escapeCharacters: ['"', '\\'] as char[])
    }
  }

  @Test
  void testPrefix() throws Exception {
    engine.remoteSession {
      prefix('sudo') {
        exec([
	    'ls -la',
	    'whoami'
        ])
      }
    }
  }

  @Test
  void testRemoteFile() throws Exception {
    engine.remoteSession {
      remoteFile('/etc/init.conf').text = 'content'
    }
  }

  @Test
  void testOk() throws Exception {
    engine.remoteSession {
      assert ok('whoami')
    }
  }

  @Test
  void testFail() throws Exception {
    engine.remoteSession {
      assert fail('mkdur dur')
    }
  }

}