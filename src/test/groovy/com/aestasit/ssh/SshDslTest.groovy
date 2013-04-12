package com.aestasit.ssh

import org.apache.sshd.SshServer
import org.apache.sshd.server.command.ScpCommandFactory
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.apache.sshd.server.sftp.SftpSubsystem
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import com.aestasit.ssh.dsl.SshDslEngine
import com.aestasit.ssh.log.SysOutLogger
import com.aestasit.ssh.mocks.MockCommandFactory
import com.aestasit.ssh.mocks.MockFileSystemFactory
import com.aestasit.ssh.mocks.MockShellFactory
import com.aestasit.ssh.mocks.MockUserAuthFactory

/**
 * SSH DSL test case that verifies different DSL syntax use cases.
 *
 * @author Andrey Adamovich
 *
 */
class SshDslTest {

  static SshServer sshd
  static SshOptions options
  static SshDslEngine engine

  @BeforeClass
  def static void startSshd() {
    sshd = SshServer.setUpDefaultServer()
    sshd.with {
      port = 2233
      keyPairProvider = new SimpleGeneratorHostKeyProvider()
      commandFactory = new ScpCommandFactory( new MockCommandFactory() )
      shellFactory = new MockShellFactory()
      userAuthFactories = [new MockUserAuthFactory()]
      fileSystemFactory = new MockFileSystemFactory()
      subsystemFactories = [
        new SftpSubsystem.Factory()
      ]
    }
    sshd.start()
  }

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

      execOptions.with {
        showOutput = true
        failOnError = false
        succeedOnExitStatus = 0
        maxWait = 30000
        outputFile = new File("output.file")
        appendFile = true
      }

      scpOptions.with { verbose = true }
    }
    engine = new SshDslEngine(options)
  }

  def static File getCurrentDir() {
    return new File(".").getAbsoluteFile()
  }

  def static File getTestFile() {
    return new File("input.file").getAbsoluteFile()
  }

  @AfterClass
  def static void stopSshd() {
    sshd?.stop(true)
  }

  @Test
  def void testDefaultSettings() throws Exception {
    engine.remoteSession {
      exec 'whoami'
      exec 'du -s'
      exec 'rm -rf /tmp/test.file'
      scp testFile, '/tmp/test.file'
    }
  }

  @Test
  def void testUrlAndOverriding() throws Exception {
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
  def void testMethodOverriding() throws Exception {
    // Test overriding default connection settings through method parameter.
    engine.remoteSession('user2:654321@localhost:2233') {

      exec 'whoami'
      exec 'du -s'
      exec 'rm -rf /tmp/test.file'
      scp testFile, '/tmp/test.file'

    }
  }

  @Test
  def void testPropertyOverriding() throws Exception {
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
  def void testOutputScripting() throws Exception {
    // Test saving the output and setting exec parameters through a builder.
    engine.remoteSession {
      println ">>>>> COMMAND: whoami"
      def output = exec(command: 'whoami', showOutput: false)
      output.output.eachLine { line -> println ">>>>> OUTPUT: ${line.reverse()}" }
      println ">>>>> EXIT: ${output.exitStatus}"
    }
  }

  @Test
  def void testFailOnError() throws Exception {
    engine.remoteSession {
      exec(command: 'abcd', failOnError: false)
    }
  }

  @Test
  def void testTimeout() throws Exception {
    try {
      engine.remoteSession {
        exec(command: 'timeout', maxWait: 1000)
      }
    } catch (SshException e) {
      assert e.cause.message.contains('timeout')
    }
  }

  @Test
  def void testExecClosure() throws Exception {
    // Test closure based builder for exec.
    engine.remoteSession { exec { command = 'whoami' } }
  }

  @Test
  def void testCopy() throws Exception {
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
  def void testMultiExec() throws Exception {
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
  def void testPrefix() throws Exception {
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
  def void testRemoteFile() throws Exception {
    engine.remoteSession {
      remoteFile('/etc/init.conf').text = 'content'
    }
  }

}