/*
 * This script tests sudo upload functionality.
 *
 */

@GrabResolver(name='snapshot', root='http://oss.sonatype.org/content/groups/public')
@Grab( group = 'com.aestasit.infrastructure.sshoogr', module = 'sshoogr', version = '0.9.11-SNAPSHOT', changing = true)
import static com.aestasit.ssh.DefaultSsh.*

options.scpOptions {
  uploadToDirectory = '/tmp'
  postUploadCommand = 'sudo cp -R %from%/* %to% && sudo rm -rf %from%'
}

remoteSession {
  user = 'ec2-user'
  keyFile = new File('secret.pem')
  host = 'ec2-54-216-184-163.eu-west-1.compute.amazonaws.com'
  connect()
  println "====================================================="
  scp {
    into { remoteDir("/usr/bin") }
    from { localFile('./sudoUpload.groovy') }
  }
  println "====================================================="
  scp {
    into { remoteDir("/usr/bin") }
    from { localDir('.') }
  }
  println "====================================================="
  exec 'ls -la /usr/bin/*.groovy'
  println "====================================================="
  exec 'cat /usr/bin/sudoUpload.groovy'
  println "====================================================="
  remoteFile('/usr/bin/sudoUpload.groovy').text = 'It works!!!'
  println "====================================================="
  exec 'cat /usr/bin/sudoUpload.groovy'
}

