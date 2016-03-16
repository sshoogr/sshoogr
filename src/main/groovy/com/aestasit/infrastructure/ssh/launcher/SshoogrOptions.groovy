/*
 * Copyright (C) 2011-2015 Aestas/IT
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

import com.lexicalscope.jewel.cli.Option
import com.lexicalscope.jewel.cli.Unparsed

/**
 * Sshoogr options parsed from command-line parameters.
 *
 * @author Andrey Adamovich
 *
 */
interface SshoogrOptions {

  @Option(defaultValue = [ '127.0.0.1' ], shortName = [ 'h' ], description = "default host")
  String getHost()

  @Option(defaultValue = [ 'root' ], shortName = [ 'u' ], description = "default user name")
  String getUser()

  @Option(defaultToNull = true, shortName = [ 'i' ], description = "path to default key file")
  File getKey()

  @Option(defaultToNull = true, shortName = [ 'p' ], description = "default password")
  String getPassword()

  @Option(defaultValue = [ "22" ], description = "default port")
  int getPort()

  @Option(description = "trust unknown hosts")
  boolean isTrust()

  @Unparsed(defaultValue = [ 'default.sshoogr' ] )
  Collection<File> getInputFiles()

}
