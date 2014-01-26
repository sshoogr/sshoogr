/*
 * This script tests sudo upload functionality.
 *
 */

@GrabResolver(name='snapshot', root='http://oss.sonatype.org/content/groups/public')
@Grab( group = 'com.aestasit.infrastructure.sshoogr', module = 'sshoogr', version = '0.9.14-SNAPSHOT', changing = true)
import static com.aestasit.ssh.DefaultSsh.*

options.execOptions {
  prefix = 'sudo '
}

options.scpOptions {
  uploadToDirectory = '/tmp'
  postUploadCommand = 'sudo cp -R %from%/* %to% && sudo rm -rf %from%'
}

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
      baseurl=${settings.puppetProductsRepository ?: pluginSettings.repositories.puppetProducts}
      enabled=1
      gpgcheck=0
      ${proxySettings}

      [puppet-deps]
      name=Puppet Dependencies
      baseurl=${settings.puppetDependenciesRepository ?: pluginSettings.repositories.puppetDependencies}
      enabled=1
      gpgcheck=0
      ${proxySettings}
    """
}

