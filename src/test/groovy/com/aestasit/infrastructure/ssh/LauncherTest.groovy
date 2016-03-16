package com.aestasit.infrastructure.ssh

import com.aestasit.infrastructure.ssh.launcher.Sshoogr
import org.junit.Rule
import org.junit.Test
import org.junit.contrib.java.lang.system.ExpectedSystemExit

class LauncherTest extends BaseSshTest {

  @Rule
  public final ExpectedSystemExit exit = ExpectedSystemExit.none();

  static final String script = '''
    remoteSession {
      exec 'whoami'
      exec 'du -s'
      exec 'rm -rf /tmp/test.file'
      remoteFile('/etc/init.conf').text = 'content'
    }
    '''

  @Test
  void scriptWithConnectionParameters() throws Exception {
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

  @Test
  void defaultSshoogrScript() throws Exception {
    Sshoogr.main()
  }

  @Test
  void helpMessage() throws Exception {
    exit.expectSystemExitWithStatus(1)
    String output = captureOutput {
      Sshoogr.main(['--help'] as String[])
    }
    assert output.contains('Usage:')
  }

  @Test
  void notExistingScript() throws Exception {
    exit.expectSystemExitWithStatus(127)
    Sshoogr.main(['gg.sshoogr'] as String[])
  }

  static File getTemporaryScript() {
    File scriptFile = File.createTempFile("default", ".sshoogr")
    scriptFile.text = script
    scriptFile
  }

}
