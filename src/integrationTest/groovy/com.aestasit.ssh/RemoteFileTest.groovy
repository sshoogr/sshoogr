package com.aestasit.ssh

import com.aestasit.ssh.dsl.SshDslEngine
import com.aestasit.ssh.log.SysOutLogger
import org.junit.BeforeClass
import org.junit.Test

class RemoteFileTest {

  static SshOptions options
  static SshDslEngine engine

  @BeforeClass
  def static void defineOptions() {
    options = new SshOptions()
    options.with {

      logger = new SysOutLogger()

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
