# groovy-ssh-dsl

## Overview

The `groovy-ssh-dsl` is a **Groovy**-based **DSL** library for working with remote **SSH** servers. The **DSL** allows connecting, 
executing remote commands, coping files and directories, creating tunnels in a simple and concise way.

The library was jointly developed by **Aestas/IT** (http://aestasit.com) and **NetCompany A/S** (http://www.netcompany.com/) 
to support quickly growing operations and hosting department.

## Usage

### Creating SshDslEngine instance

The library's classes are `SshDslEngine` and `SshOptions`, which obviously need to be imported before the library can be used:  

    import com.aestasit.ssh.dsl.SshDslEngine
    import com.aestasit.ssh.SshOptions

To create a simple instance of the engine with default options you can just use the following line: 
    
    def engine = new SshDslEngine(new SshOptions())

### Basic usage

The entry point for using the **DSL** is the `remoteSession` method, which accepts an **SSH** **URL** and a closure with **Groovy** or **DSL** code:

    engine.remoteSession('user2:654321@localhost:2222') {
      exec 'rm -rf /tmp/*'  
      exec 'touch /var/lock/my.pid'
      remoteFile('/var/my.conf').text = "enabled=true" 
    } 

More examples and explanations can be found in the following sections.

### Remote connections

The `remoteSession` method accepts an **SSH** **URL** and a closure, for example: 

    engine.remoteSession("user:password@localhost:22") {
      ...
    }

Inside the closure you can execute remote commands, access remote file content, upload and download files, create tunnels. 

If your connection settings were set with the help of default configuration (see "Configuration options" section), 
then you can omit **URL** parameter: 

    engine.remoteSession {
      ...
    }

You can also override the defaults in each session by directly assigning `host`, `username`, `password` and `port` properties:

    engine.remoteSession {

      host = 'localhost'
      username = 'user2'
      password = '654321'
      port = 2222
    
      ...
    
    }

Also you can assign **SSH** **URL** to the `url` property instead:

    engine.remoteSession {
    
      url = 'user2:654321@localhost:2222'
    
      ...
    
    }

Actual connection to the remote host will be made upon first command or file access, and, naturally, connection will be 
automatically closed after code block finishes. But you can explicitly call `connect` or `disconnect` methods to control this:

    engine.remoteSession {
    
      // explicitly call connect 
      connect()     
      
      // do some stuff
      ...
    
      // explicitly disconnect
      disconnect()
    
      // explicitly connect again
      connect()     
    
      ...
     
    }
    
In next section, we will see how to execute remote commands.    

### Executing commands

The simplest way to execute a command within a remote session is by using `exec` method that just takes a command string:

    engine.remoteSession {
      exec 'ls -la'
    }

You can also pass a list of commands in an array:

    exec([
     'ls -la', 
     'date'
    ])

The `exec` behavior can also be controlled with additional named parameters given to the method. For example, in order 
to hide commands output you can use the following syntax:

    exec(command: 'ls –la', showOutput: false)

Parameter names match the ones specified in "Configuration options" section for the `execOptions`, and all 
can be used to override default settings for specific commands.

In the same way, you can also define common parameters for a block of commands passed as an array:

    exec(showOutput: false, command: [
     'ls -la', 
     'date'
    ])

Also you can get access to command output, exit code and exception thrown during command execution. This can be useful 
for implementing logic based on a result returned by the remote command and/or parsing of the output. For example,

    def result = exec(command: '/usr/bin/mycmd', faileOnError: false, showOutput: false)
    if (result.exitStatus == 1) {
      result.output.eachLine { line ->
        if (line.contains('WARNING')) {
          throw new RuntimeException("Warning!!!")
        }
      }
    }

Another 2 methods that you can use around your commands are `prefix` and `suffix`. They are similar to using `prefix` 
and `suffix` options in `execOptions` or named parameters to `exec` method.

    prefix("sudo") {
      exec 'ls -la'
      exec 'df -h'
    }

And with `suffix`:

    suffix(">> output.log") {
      exec 'ls -la'
      exec 'df -h'
      exec 'date'
      exec 'facter'
    }

 
### File uploading/downloading

The simplest way to modify remote text file content is by using `remoteFile` method, which returns remote 
file object instance, and assign some string to the `text` property:

    remoteFile('/etc/yum.repos.d/puppet.repo').text = '''
      [puppet]
      name=Puppet Labs Packages
      baseurl=http://yum.puppetlabs.com/el/$releasever/products/$basearch/
      enabled=0
      gpgcheck=0
    '''

Each line of the input string will be trimmed before it's copied to the remote file.
For text file downloading you can just read the `text` property:

    println remoteFile('/etc/yum.repos.d/puppet.repo').text

Single file uploading can be done in the following way:

    scp "$buildDir/test.file", '/tmp/test.file'

This method only works for file uploading (from local environment to remote). You can also write the example above 
in more verbose form with the help of closures: 

    scp {
      from { localFile "$buildDir/test.file" }
      into { remoteFile '/tmp/test.file' }
    }

If you need to upload a directory or a set of several files that you need to use the same closure-based structure, 
but with the help of `remoteDir` and `localDir` methods:

    scp {
      from { localDir "$buildDir/application" }
      into { remoteDir '/var/bea/domain/application' }
    }

In similar way you can download directories and files:

    scp {
      from { remoteDir '/etc/nginx' }
      into { localDir "$buildDir/nginx" }
    }

You can also copy multiple sources into multiple targets:

    scp {
      from { 
        localDir "$buildDir/doc" 
        localFile "$buildDir/readme.txt" 
        localFile "$buildDir/license/license.txt" 
      }
      into { 
        remoteDir '/var/server/application' 
        remoteDir '/repo/company/application'
      }
    }

During upload/download operation target local and remote directories will be created automatically.

### Tunneling

If inside your build script you need to get access to a remote server that is not visible directly from the local 
machine, then you can create a tunnel to that server by using `tunnel` method:

    tunnel('1.2.3.4', 8080) { int localPort ->
      ...
    }

All code executed within the closure passed to the tunnel method will have access to server tunnel running on `localhost` 
and randomly selected `localPort`, which is passed as a parameter to the closure. Inside that tunnel code you can, for 
example, deploy a web application or send some **HTTP** command to remote server:

    tunnel('1.2.3.4', 8080) { int localPort ->
      def result = new URL("http://localhost:${localPort}/flushCache").text
      if (result == 'OK') {
        println "Cache is flushed!"
      } else {
        throw new RuntimeException(result)
      }
    }

Tunnel will be closed upon closure completion.
Also you can define local port yourself in the following way:

    tunnel(7070, '1.2.3.4', 8080) { 
      def result = new URL("http://localhost:7070/flushCache").text
      ...
    }

### Configuration options

The following list gives an overview of the available configuration options:

 - `defaultHost`, `defaultUser`, `defaultPassword`, `defaultPort` (defaults to 22) - Default host, user name, password or port to use in remote connection in case they are not specified in some other way (through `url`, `host`, `port`, `user` or `password` properties inside `remoteSession` method).
 - `defaultKeyFile` - Default key file to use in remote connection in case it is not specified through keyFile property inside remoteSession method. Key file is an alternative mechanism to using passwords.
 - `failOnError` (defaults to true) - If set to true, failed remote commands and file operations will fail the build.
 - `verbose` (defaults to false) - If set to true, library produces more debug output.
 
The `sshOptions` may also contain a nested `execOptions` structure, which defines remote command execution (see 
"Executing commands" section) options. It has the following properties:

 - `showOutput` (defaults to true) - If set to true, remote command output is printed.
 - `showCommand` (defaults to true) - If set to true, remote command is printed.
 - `maxWait` (defaults to 0) - Number of milliseconds to wait for command to finish. If it is set to 0, then library will wait forever.
 - `succeedOnExitStatus` (defaults to 0) - Exit code that indicates commands success. If command returns different exit code, then build will fail.
 - `outputFile` - File, to which to send command's output. 
 - `appendFile` (defaults to false) - If outputFile is specified, then this option indicates if data should be appended or file should be created from scratch.
 - `failOnError` (defaults to true) - If set to true, failed remote commands will fail the build.
 - `verbose` (defaults to false) - If set to true, library produces more debug output.
 - `prefix` - String to prepend to each executed command, for example, "`sudo`".
 - `suffix` - String to append to each executed command, for example, "`>> output.log`".

There is also a nested `scpOptions` structure, which defines remote file copying (see "File uploading/downloading" 
section) options. It has the following properties:

 - `failOnError` (defaults to true) - If set to true, failed file operations will fail the build.
 - `showProgress` (defaults to false) - If set to true, library shows additional information regarding file upload/download progress.
 - `verbose` (defaults to false) - If set to true, library produces more debug output.

### Populating SshOptions

A more verbose example of creating `SshOptions` object (demonstrating most of available options) can be found below: 

    import com.aestasit.ssh.log.SysOutLogger

    ...

    options = new SshOptions()
    options.with {

      logger = new SysOutLogger()

      defaultHost = '127.0.0.1'
      defaultUser = 'user1'
      defaultPassword = '123456'
      defaultPort = 2233

      reuseConnection = true
      trustUnknownHosts = true

      execOptions.with {
        showOutput = true
        failOnError = false
        succeedOnExitStatus = 0
        maxWait = 30000
        outputFile = new File("output.file")
        appendFile = true
      }

      scpOptions.with { 
        verbose = true
        showProgress = true 
      }
      
    }
        