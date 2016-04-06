# sshoogr

## Overview

The `sshoogr` is a **Groovy**-based **DSL** library for working with remote servers through **SSH**. The **DSL** allows:

- connecting,
- executing remote commands,
- copying files and directories,
- creating tunnels in a simple and concise way.

The library was jointly developed by **Aestas/IT** (http://aestasit.com) and **NetCompany A/S** (http://www.netcompany.com/) to support the quickly growing company's operations and hosting department.

### Installing `sshoogr` with SDKMAN!

The simplest way to use `sshoogr` from the command line is by using [SDKMAN!](http://sdkman.io).

If SDKMAN is not yet installed, open a terminal and enter the following:

    $ curl -s get.sdkman.io | bash

Follow the instructions presented in the terminal, then enter the following command:

    $ sdk install sshoogr

This will install the `sshoogr` and make it available on your path.

### Using `sshoogr` in Groovy scripts

The easiest way to use `sshoogr` in a **Groovy** script is by importing the dependency using [Grape](http://groovy.codehaus.org/Grape).

```groovy
@Grab('com.aestasit.infrastructure.sshoogr:sshoogr:0.9.25')
import static com.aestasit.infrastructure.ssh.DefaultSsh.*
```

The entry point for using the **DSL** is the `remoteSession` method, which accepts an **SSH** **URL** and a closure with **Groovy** or **DSL** code:

```groovy
remoteSession('user2:654321@localhost:2222') {
  exec 'rm -rf /tmp/*'
  exec 'touch /var/lock/my.pid'
  remoteFile('/var/my.conf').text = "enabled=true"
}
```

For more use cases, please refer to the following sections or to the `examples` folder in this repository.

### Remote connections

The `remoteSession` method accepts an **SSH** **URL** and a closure, for example:

```groovy
remoteSession("user:password@localhost:22") {
  //...
}
```

Inside the closure you can execute remote commands, access remote file content, upload and download files, create tunnels.

If your connection settings were set with the help of default configuration (see "Configuration options" section),
then you can omit the **URL** parameter:

```groovy
remoteSession {
  //...
}
```

Furthermore, it is possible to override the default values in each session by directly assigning `host`, `username`, `password` and `port` properties:

```groovy
remoteSession {

  host = 'localhost'
  username = 'user2'
  password = '654321'
  port = 2222

  //...

}
```

The **SSH**'s **URL** can be also assigned from withing the remote session declaration, like so:

```groovy
remoteSession {

  url = 'user2:654321@localhost:2222'

  //...

}
```

The actual connection to the remote host will be executed upon the first command or file access, and, naturally, the connection will be
automatically closed after the code block terminates.

You can explicitly call `connect` or `disconnect` methods to control this behavior:

```groovy
remoteSession {

  // explicitly call connect
  connect()

  // do some stuff ...

  // explicitly disconnect
  disconnect()

  // explicitly connect again
  connect()

  //...

}
```

In the next section, we will see how to execute remote commands.

### Executing commands

The simplest way to execute a command within a remote session is by using the `exec` method that just takes a command string:

```groovy
remoteSession {
  exec 'ls -la'
}
```

You can also pass a list of commands in an array:

```groovy
exec([
  'ls -la',
  'date'
])
```

The `exec` behavior can also be controlled with additional named parameters given to the method. For example, in order
to hide commands output, you can use the following syntax:

```groovy
exec(command: 'ls –la', showOutput: false)
```

The additional Parameter names are specified in the "Configuration options" section for the `execOptions`. They can all
be used to override default settings for specific commands.

In the same way, you can also define common parameters for a block of commands passed as an array:

```groovy
exec(showOutput: false, command: [
  'ls -la',
  'date'
])
```

Also you can get access to command output, exit code and exception thrown during command execution. This can be useful
for implementing logic based on a result returned by the remote command and/or parsing of the output. For example,

```groovy
def result = exec(command: '/usr/bin/mycmd', failOnError: false, showOutput: false)
if (result.exitStatus == 1) {
  result.output.eachLine { line ->
    if (line.contains('WARNING')) {
      throw new RuntimeException("Warning!!!")
    }
  }
}
```

Two additional methods that you can use around your commands are `prefix` and `suffix`. They are similar to using the `prefix`
and `suffix` options in `execOptions` or named parameters to `exec` method.

```groovy
prefix("sudo") {
  exec 'ls -la'
  exec 'df -h'
}
```

And with `suffix`:

```groovy
suffix(">> output.log") {
  exec 'ls -la'
  exec 'df -h'
  exec 'date'
  exec 'facter'
}
```
 
### File uploading/downloading

The simplest way to modify a remote text file content is by using `remoteFile` method, which returns a remote
file object instance, and assign some string to the `text` property:

```groovy
remoteFile('/etc/yum.repos.d/puppet.repo').text = '''
  [puppet]
  name=Puppet Labs Packages
  baseurl=http://yum.puppetlabs.com/el/$releasever/products/$basearch/
  enabled=0
  gpgcheck=0
'''
```

Each line of the input string will be trimmed before it's copied to the remote file.
For text file downloading you can just read the `text` property:

```groovy
println remoteFile('/etc/yum.repos.d/puppet.repo').text
```

Uploading of a single file can be done in the following way:

```groovy
scp "$buildDir/test.file", '/tmp/test.file'
```

This method only works for file uploading (from local environment to remote). You can also write the example above
in a more verbose form with the help of closures:

```groovy
scp {
  from { localFile "$buildDir/test.file" }
  into { remoteFile '/tmp/test.file' }
}
```

A whole directory can be uploaded by using the `remoteDir` and `localDir` methods of `scp`.

```groovy
scp {
  from { localDir "$buildDir/application" }
  into { remoteDir '/var/bea/domain/application' }
}
```

In similar fashion, you can download directories and files:

```groovy
scp {
  from { remoteDir '/etc/nginx' }
  into { localDir "$buildDir/nginx" }
}
```

You can also copy multiple sources into multiple targets:

```groovy
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
```

During any upload/download operation, local and remote directories will be created automatically.

### Tunneling

If inside your build script you need to get access to a remote server that is not visible directly from the local
machine, then you can create a tunnel to that server by using the `tunnel` method:

```groovy
tunnel('1.2.3.4', 8080) { int localPort ->
  //...
}
```

All code executed within the closure passed to the tunnel method will have access to a server tunnel running on `localhost`
and randomly selected `localPort`, which is passed as a parameter to the closure. Inside that tunnel code you can, for
example, deploy a web application or send some **HTTP** command to remote server:

```groovy
tunnel('1.2.3.4', 8080) { int localPort ->
  def result = new URL("http://localhost:${localPort}/flushCache").text
  if (result == 'OK') {
    println "Cache is flushed!"
  } else {
    throw new RuntimeException(result)
  }
}
```

The tunnel will be closed upon closure completion.
Also, you can define a local port yourself in the following way:

```groovy
tunnel(7070, '1.2.3.4', 8080) {
  def result = new URL("http://localhost:7070/flushCache").text
  //...
}
```

### Configuration options

The following list gives an overview of the available configuration options:

 - `defaultHost`, `defaultUser`, `defaultPassword`, `defaultPort` (defaults to 22) - Default host, user name, password or port to use in remote connection in case they are not specified in some other way (through `url`, `host`, `port`, `user` or `password` properties inside the `remoteSession` method).
 - `defaultKeyFile` - Default key file to use in remote connection in case it is not specified through the `keyFile` property inside the `remoteSession` method.
 - `failOnError` (defaults to true) - If set to true, failed remote commands and file operations will throw an exception.
 - `verbose` (defaults to false) - If set to true, the library produces more debug output.

The `sshOptions` may also contain a nested `execOptions` structure, which defines remote command execution (see
"Executing commands" section) options. It has the following properties:

 - `showOutput` (defaults to true) - If set to true, remote command output is printed.
 - `showCommand` (defaults to true) - If set to true, remote command is printed.
 - `hideSecrets` (defaults to true) - If set to true, secret Strings contained in ExecOptions.secrets will be redacted from output and replaced by `********`.
 - `secrets` (defaults to [ ]) - a list of secret Strings to be redacted from output
 - `maxWait` (defaults to 0) - Number of milliseconds to wait for command to finish. If it is set to 0, then library will wait forever.
 - `succeedOnExitStatus` (defaults to 0) - Exit code that indicates commands success. If command returns different exit code, then build will fail.
 - `failOnError` (defaults to true) - If set to true, failed remote commands will fail the build.
 - `verbose` (defaults to false) - If set to true, library produces more debug output.
 - `prefix` - String to prepend to each executed command, for example, "`sudo`".
 - `suffix` - String to append to each executed command, for example, "`>> output.log`".

There is also a nested `scpOptions` structure, which defines remote file copying options (see "File uploading/downloading"
section). It has the following properties:

 - `failOnError` (defaults to true) - If set to true, failed file operations will fail the build.
 - `showProgress` (defaults to false) - If set to true, library shows additional information regarding file upload/download progress.
 - `verbose` (defaults to false) - If set to true, library produces more debug output.

### Advanced usage

#### Creating a `SshDslEngine` instance

If you need to embed `sshoogr` into your own **DSL** or another library you may need to use internal classes instead of default static methods. The main library's classes are `SshDslEngine` and `SshOptions`, which need to be imported before the library can be used:

```groovy
import com.aestasit.infrastructure.ssh.dsl.SshDslEngine
import com.aestasit.infrastructure.ssh.SshOptions
```

To create a simple instance of the engine with the default options you can just use the following instruction:

```groovy
def engine = new SshDslEngine(new SshOptions())
```
    
The `engine` instance gives access to the `remoteSession` method.

#### Populating `SshOptions`

A more verbose example of creating a `SshOptions` object can be found below:

```groovy
import com.aestasit.infrastructure.ssh.log.SysOutLogger

//...

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
    hideSecrets = true
    secrets = ['secret1']
    succeedOnExitStatus = 0
    maxWait = 30000
  }

  scpOptions.with {
    verbose = true
    showProgress = true
  }

}
```
