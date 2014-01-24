/*
 * This script tests sudo upload functionality.
 *
 */

@GrabResolver(name='snapshot', root='http://oss.sonatype.org/content/groups/public')
@Grab( group = 'com.aestasit.infrastructure.sshoogr', module = 'sshoogr', version = '0.9.10-SNAPSHOT', changing = true)
import static com.aestasit.ssh.DefaultSsh.*

options.scpOptions {
  uploadToDirectory = '/tmp'
  postUploadCommand = 'sudo cp -R %from%/* %to% ; sudo rm -rf %from%'
}

remoteSession {
  user = 'ec2-user'
  keyFile = new File('secret.pem')
  host = 'ec2-54-216-184-163.eu-west-1.compute.amazonaws.com'
  scp {
    into { remoteDir("/usr/bin") }
    from { localFile('./sudoUpload.groovy/') }
  }
  exec 'ls -la /usr/bin/*.groovy'
  //remoteFile('/usr/bin/sudoUpload.groovy').text = 'It works!!!'
}

