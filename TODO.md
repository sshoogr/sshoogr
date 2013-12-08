* Add better logging control
* Allow JSCH debug logging
* Implement test for tunneling
* Implement test for su
* Implement test for file downloading
* Add more asserts to the tests
* Increase coverage to 80%
* Add support for parallel execution of the commands
* follow simlinks
* custom local ip address for tunnels
* implement withStream uploading for remote file
* implement withPrinter for remote file
* implement leftShift for remote file
* implement permissions, owner, touch methods for remote file
* during logging show relative paths instead of absolute
* normalize slashes in the path during logging
* is it possible to reuse exec/sftp channels within session?
* implement role filtering a la capistrano
* implement command block definitions to be able to execute them later
* implement rollback mechanism in exec, scp, and blocks
* implement scp copy through tmp directory
