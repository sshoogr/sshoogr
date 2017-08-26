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

package com.aestasit.infrastructure.ssh.launcher

import com.lexicalscope.jewel.cli.CommandLineInterface
import com.lexicalscope.jewel.cli.Option
import com.lexicalscope.jewel.cli.Unparsed
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * Sshoogr options parsed from command-line parameters.
 *
 * @author Andrey Adamovich
 *
 */
@CommandLineInterface(application = 'sshoogr')
@CompileStatic
@TypeChecked
interface SshoogrOptions {

  @Option(defaultValue = ['127.0.0.1'], shortName = ['h'], description = 'Default host to connect to. Default value is "127.0.0.1".')
  String getHost()

  @Option(defaultValue = ['root'], shortName = ['u'], description = 'Default user name to use for connections. Default value is "root".')
  String getUser()

  @Option(defaultValue = ['standard'], shortName = ['l'], description = 'Logger implementation to use during execution. Possible options are: standard, color, slf4j. Default value is "standard".')
  String getLogger()

  @Option(defaultToNull = true, shortName = ['i'], description = 'Path to default key file to use for connections.')
  File getKey()

  @Option(defaultToNull = true, shortName = ['p'], description = 'Default password to use for connections.')
  String getPassword()

  @Option(defaultValue = ['22'], description = 'Default port to use for connections. Default value is 22.')
  int getPort()

  @Option(description = 'Trust unknown SSH hosts.')
  boolean isTrust()

  @Option(helpRequest = true, description = 'display help')
  boolean getHelp()

  @Unparsed(defaultValue = ['default.sshoogr'], name = '[scripts]', description = 'S')
  Collection<File> getInputFiles()

}
