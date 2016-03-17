@GrabResolver(name='snapshot', root='https://oss.sonatype.org/content/repositories/snapshots/')
@Grab( group = 'com.aestasit.infrastructure.sshoogr', module = 'sshoogr', version = '0.9.23-SNAPSHOT', changing = true)
import static com.aestasit.infrastructure.ssh.DefaultSsh.*
import com.aestasit.infrastructure.ssh.log.AnsiLogger

defaultHost = '192.168.33.144'
defaultUser = 'vagrant'
defaultPassword = 'vagrant'
defaultPort = 22
trustUnknownHosts = true

logger = new AnsiLogger()

remoteSession {
  exec('uname -a')
  exec('date')
  exec('hostname')
  exec {
    prefix = "sudo sh -c '"
    suffix = "'"
    escapeCharacters = ['"', "'", '\\', '/']
    command = 'printf "%s\\n%s\\n" test rest'
  }
}