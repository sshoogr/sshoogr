/*
 * This script tests sudo upload functionality.
 *
 */

@Grab('com.aestasit.infrastructure.sshoogr:sshoogr:0.9.26')
import static com.aestasit.infrastructure.ssh.DefaultSsh.*

options.execOptions {
  prefix = 'sudo'
}

options.scpOptions {
  uploadToDirectory = '/tmp'
  // postUploadCommand = 'cp -R %from%/* %to% && sudo rm -rf %from%'
}

String CR = String.valueOf(Character.toChars(0x0D))
String LF = String.valueOf(Character.toChars(0x0A))

remoteSession {
  user = 'ec2-user'
  keyFile = new File('secret.pem')
  host = 'ec2-176-34-83-31.eu-west-1.compute.amazonaws.com'
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
  println "====================================================="
  remoteFile('/etc/yum.repos.d/puppet.repo').text = """
      [puppet]
      name=Puppet Labs Packages
      baseurl=http://yum.puppetlabs.com/el/6x/products/x86_64/
      enabled=1
      gpgcheck=0

      [puppet-deps]
      name=Puppet Dependencies
      baseurl=http://yum.puppetlabs.com/el/6x/dependencies/x86_64/
      enabled=1
      gpgcheck=0
    """
  println "====================================================="
  exec 'yum install nano'
  println "====================================================="
  def hostsFileContent = remoteFile("/etc/hosts").text
  def currentHosts = hostsFileContent.readLines()
  remoteFile("/etc/hosts").text = currentHosts.join(LF)
  println "====================================================="
}

