/*
 * This script connects to remote server which hosts Subversion server. That server is behind firewall 
 * and the only way we can see it is by creating a tunnel to port 80. For each repository in the list
 * script recreates empty repository on remote machine with the help of svnadmin command. After that
 * it streams data from locally stored repository dumps into newly created remote repository through 
 * the tunnel with the help of svnrdump command.    
 * 
 */

@Grab('com.aestasit.infrastructure.sshoogr:sshoogr:0.9.4')
import static com.aestasit.infrastructure.ssh.DefaultSsh.*

def repos = [
  'project1',
  'project2',
  'project3',
  'project4',
]

def sshUser = 'root'
def sshPassword = 'rootpass'
def sshHost = '1.2.3.4'

def svnUser = 'admin'
def svnPassword = 'admin'

def LF = String.valueOf(Character.toChars(0x0A))

remoteSession("${sshUser}:${sshPassword}@${sshHost}:22") {
  tunnel('127.0.0.1', 80) { int svnPort ->
    repos.each { repo ->
      exec "rm -rf /var/www/svn/${repo}"
      exec "svnadmin create /var/www/svn/${repo}"
      remoteFile("/var/www/svn/${repo}/hooks/pre-revprop-change").text = "#!/bin/sh${LF}exit 0;"
      exec "chmod 755 /var/www/svn/${repo}/hooks/pre-revprop-change"
      exec "chown -R apache:apache /var/www/svn/${repo}"
      def read = "cat dumps/${repo}.dmp".execute()
      def write = "svnrdump load --username ${svnUser} --password ${svnPassword} http://localhost:${svnPort}/repos/${repo}".execute()
      write.consumeProcessOutput(System.out, System.err)
      read | write
      write.waitFor()
    }
  }
}

