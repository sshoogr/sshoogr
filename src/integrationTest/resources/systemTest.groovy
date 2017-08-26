/*
 * Copyright (C) 2011-2017 Aestas/IT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@GrabResolver(name='snapshot', root='https://oss.sonatype.org/content/repositories/snapshots/')
@Grab( group = 'com.aestasit.infrastructure.sshoogr', module = 'sshoogr', version = '0.9.23-SNAPSHOT', changing = true)
import static com.aestasit.infrastructure.ssh.DefaultSsh.*
import com.aestasit.infrastructure.ssh.log.AnsiEventLogger

defaultHost = '192.168.33.144'
defaultUser = 'vagrant'
defaultPassword = 'vagrant'
defaultPort = 22
trustUnknownHosts = true

logger = new AnsiEventLogger()

remoteSession {
  exec('uname -a')
  exec('date')
  exec('hostname')
  exec {
    prefix = "sudo sh -c '"
    suffix = "'"
    escapeCharacters = ['"', "'", '\\', '/']
    command = 'printf "%s\\n%s\\n" test rest'
  }
}