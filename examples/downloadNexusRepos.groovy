/*
 * This script connects to remote server, which hosts Nexus server. It zips a list of repositories 
 * and downloads zipped file locally.
 *
 */

@Grab('com.aestasit.infrastructure.sshoogr:sshoogr:0.9.21')
import static com.aestasit.infrastructure.ssh.DefaultSsh.*

def folders = [
  'thirdparty',
  'releases',
  'snapshots',
]

def sshUser = 'root'
def sshPassword = '123'
def sshHost = 'build01.server.com'

remoteSession("${sshUser}:${sshPassword}@${sshHost}:22") {
  folders.each { folder ->
    exec "zip -9 -q -r /tmp/${folder}.zip /usr/local/sonatype-work/nexus/storage/${folder}"
    scp {
      from { remoteFile("/tmp/${folder}.zip") }
      into { localDir('./repos/') }
    }
  }
}

