/*
* Copyright (C) 2011-2017 Aestas/IT
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

import com.aestasit.infrastructure.ssh.dsl.SshDslEngine
import com.aestasit.infrastructure.ssh.log.SysOutEventLogger
import org.junit.BeforeClass
import org.junit.Test

import java.util.concurrent.TimeoutException

/**
 * SSH DSL test case that verifies different DSL syntax use cases.
 *
 * @author Andrey Adamovich
 *
 */
class SshDslTest extends BaseSshTest {

  protected static SshOptions options
  protected static SshDslEngine engine

  @BeforeClass
  static void defineOptions() {
    options = new SshOptions()
    options.with {

      logger = new SysOutEventLogger()

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
        usePty = true
      }

    }
    engine = new SshDslEngine(options)
  }

  @Test
  void testDefaultSettings() {
    String output = captureOutput {
      engine.remoteSession {
        exec 'whoami'
        exec 'du -s'
        exec 'rm -rf /tmp/test.file'
        scp testFile, '/tmp/test.file'
      }
    }
    assert output.contains('whoami')
    assert output.contains('test.file')
    assert output.contains('100%')
  }

  @Test
  void testUrlAndOverriding() {
    String output = captureOutput {
      // Test overriding default connection settings through URL.
      engine.remoteSession {

        url = 'user2:654321@localhost:2233'

        exec 'whoami'
        exec 'du -s'
        exec 'rm -rf /tmp/test.file'
        scp testFile, '/tmp/test.file'

      }
    }
    assert output.contains('whoami')
    assert output.contains('test.file')
    assert output.contains('100%')
  }

  @Test
  void testPasswordlessLogin() {
    String output = captureOutput {
      engine.remoteSession {
        url = 'user2@localhost:2233'
        keyFile = testKey
        exec 'whoami'
      }
    }
    assert output.contains('whoami')
  }

  @Test
  void testMethodOverriding() {
    String output = captureOutput {
      // Test overriding default connection settings through method parameter.
      engine.remoteSession('user2:654321@localhost:2233') {

        exec 'whoami'
        exec 'du -s'
        exec 'rm -rf /tmp/test.file'
        scp testFile, '/tmp/test.file'

      }
    }
    assert output.contains('whoami')
    assert output.contains('test.file')
    assert output.contains('100%')
  }

  @Test
  void testPropertyOverriding() {
    String output = captureOutput {
      // Test overriding default connection settings through delegate parameters.
      engine.remoteSession {

        host = 'localhost'
        user = 'user2'
        password = '654321'
        port = 2233

        exec 'whoami'
        exec 'du -s'
        exec 'rm -rf /tmp/test.file'
        scp testFile, '/tmp/test.file'

      }
    }
    assert output.contains('whoami')
    assert output.contains('test.file')
    assert output.contains('100%')
  }

  @Test
  void testOutputScripting() {
    String output = captureOutput {
      // Test saving the output and setting exec parameters through a builder.
      engine.remoteSession {
        def commandResult = exec(command: 'whoami', showOutput: false)
        commandResult.output.eachLine { line -> System.out.println ">>>>> OUTPUT: ${line.reverse()}" }
      }
    }
    assert output.contains('whoami')
    assert output.contains('toor')
  }

  @Test
  void testFailedStatus() {
    engine.remoteSession {
      assert exec('i should fail!').failed()
    }
  }

  @Test
  void testFailOnError() {
    String output = captureOutput {
      engine.remoteSession {
        exec(command: 'abcd', failOnError: false)
      }
    }
    assert output.contains('abcd')
  }

  @Test
  void testTimeout() {
    try {
      engine.remoteSession {
        exec(command: 'timeout', maxWait: 1000)
      }
    } catch (SshException e) {
      assert e.cause.message.contains('timeout')
    }
  }

  @Test
  void testExecClosure() {
    String output = captureOutput {
      // Test closure based builder for exec.
      engine.remoteSession { exec { command = 'whoami' } }
    }
    assert output.contains('whoami')
  }

  @Test
  void testCopy() {
    String output = captureOutput {
      engine.remoteSession {
        scp {
          from {
            localDir new File(currentDir, 'test-settings')
          }
          into { remoteDir '/tmp/puppet' }
        }
      }
    }
    assert output.contains('100%')
  }

  @Test
  void testSudoCopy() {
    String output = captureOutput {
      engine.remoteSession {
        scp {
          uploadToDirectory = '/tmp'
          from {
            localDir new File(currentDir, 'test-settings')
          }
          into { remoteDir '/etc/puppet' }
        }
      }
    }
    assert output.contains('100%')
  }

  @Test
  void testMultiExec() {
    String output = captureOutput {
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
    assert output.contains('whoami')
    assert output.contains('ls -la')
  }


  @Test
  void testEscaping() {
    String output = captureOutput {
      engine.remoteSession {
        exec(command: 'ls -la "\\', escapeCharacters: '"\\')
        exec(command: 'ls -la "\\', escapeCharacters: ['"', '\\'])
        exec(command: 'ls -la "\\', escapeCharacters: ['"', '\\'] as char[])
      }
    }
    assert output.contains('ls -la')
  }

  @Test
  void testRedacting() {
    String output = captureOutput {
      engine.remoteSession {
        exec(command: './script.sh username secret1 secret2', secrets: ['secret1', 'secret2'])
      }
    }
    assert !output.contains('secret1')
    assert !output.contains('secret2')
  }

  @Test
  void testPrefix() {
    String output = captureOutput {
      engine.remoteSession {
        prefix('sudo') {
          exec([
            'ls -la',
            'whoami'
          ])
        }
      }
    }
    assert output.contains('sudo whoami')
    assert output.contains('sudo ls -la')
  }

  @Test
  void testRemoteFile() {
    String output = captureOutput {
      engine.remoteSession {
        remoteFile('/etc/init.conf').text = 'content'
      }
    }
    assert output.contains('100%')
  }

  @Test
  void testRemoteFileIsFile() {
    String output = captureOutput {
      engine.remoteSession {
        assert remoteFile('/etc/init.conf').file
        assert !remoteFile('/etc/init.conf').directory
        assert remoteFile('/etc').directory
        assert !remoteFile('/etc').file
      }
    }
    assert !output.contains('etc')
  }

  @Test
  void testOk() {
    String output = captureOutput {
      engine.remoteSession {
        assert ok('whoami')
      }
    }
    assert !output.contains('whoami')
  }

  @Test
  void testOkWithPrefix() {
    String output = captureOutput {
      engine.remoteSession {
        prefix 'sudo', {
          assert ok('which service')
        }
      }
    }
    assert !output.contains('which')
    assert !output.contains('sudo')
  }

  @Test
  void testFail() {
    String output = captureOutput {
      engine.remoteSession {
        assert fail('mkdur dur')
      }
    }
    assert !output.contains('mkdur')
  }

  @Test
  void testExecGStringCommand() {
    String output = captureOutput {
      def cmd = 'whoami'
      engine.remoteSession {
        assert exec(command: "$cmd", showOutput: true).output.trim() == 'root'
      }
    }
    assert output.contains('whoami')
  }

  @Test
  void testExecGStringCommandArray() {
    String output = captureOutput {
      def cmd = 'whoami'
      List cmds = ["$cmd", "$cmd"]
      engine.remoteSession {
        exec(command: cmds, showOutput: true)
      }
    }
    assert output.contains('whoami')
  }

  @Test
  void testExceptionInClosure() {
    engine.remoteSession {
      connect()
      disconnect()
    }
    printThreadNames('THREADS BEFORE:')
    try {
      engine.remoteSession {
        exec 'whoami'
        throw new TimeoutException('Bang!')
      }
    } catch (e) {
      assert e instanceof TimeoutException
      printThreadNames('THREADS AFTER:')
      List<String> threadsAfter = Thread.allStackTraces.collect { key, value -> key.name }
      assert !threadsAfter.contains('Connect thread 127.0.0.1 session')
    }
  }

  @Test
  void testExecMapValidation() {
    engine.remoteSession {
      try {
        exec commandS: 'whoami'
        fail('Should not accept missing command parameter')
      } catch (SshException e) {
        assert e.message == 'The "command" parameter is not specified!'
      }
      exec command: 'whoami'
    }
  }

  @Test
  void testOptionsOverride() {
    String output = captureOutput {
      engine.remoteSession {
        scp {
          showProgress = false
          from {
            localDir new File(currentDir, 'test-settings')
          }
          into { remoteDir '/tmp/puppet' }
        }
      }
    }
    assert !output.contains('bytes transferred')
  }

}