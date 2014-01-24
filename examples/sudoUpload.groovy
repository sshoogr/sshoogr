/*
 * This script tests sudo upload functionality.
 *
 */

@GrabResolver(name='snapshot', root='http://oss.sonatype.org/content/groups/public')
@Grab('com.aestasit.infrastructure.sshoogr:sshoogr:0.9.6-SNAPSHOT')
import static com.aestasit.ssh.DefaultSsh.*

def sshUser = 'pi'
def sshPassword = 'raspberry'
def sshHost = '192.168.1.5'

options.scpOptions {
  uploadToDirectory = '/tmp'
}

remoteSession("${sshUser}:${sshPassword}@${sshHost}:22") {
  scp {
    into { remoteDir("/usr/bin") }
    from { localFile('./sudoUpload.groovy/') }
  }
}

