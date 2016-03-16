package com.aestasit.infrastructure.ssh

import com.aestasit.infrastructure.ssh.launcher.Sshoogr
import org.junit.Test

class LauncherTest extends BaseSshTest {

  static final String script = '''
    remoteSession {
      exec 'whoami'
      exec 'du -s'
      exec 'rm -rf /tmp/test.file'
      remoteFile('/etc/init.conf').text = 'content'
    }
    '''

  @Test
  void testLauncherParameterOverride() throws Exception {
    Sshoogr.main([

      "--user",
      "user2",

      "--password",
      "654321",

      "--host",
      "localhost",

      "--trust",

      "--port",
      "2233",

      temporaryScript.absolutePath

    ] as String[])
  }

  static File getTemporaryScript() {
    File scriptFile = File.createTempFile("default", ".sshoogr")
    scriptFile.text = script
    scriptFile
  }

}
