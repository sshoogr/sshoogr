/*
 * Copyright (C) 2011-2016 Aestas/IT
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

package com.aestasit.infrastructure.ssh

import com.aestasit.infrastructure.ssh.dsl.SshDslEngine
import org.junit.Before

import static com.aestasit.infrastructure.ssh.DefaultSsh.ansi

class BaseIntegrationTest {

  protected SshOptions options
  protected SshDslEngine engine

  @Before
  void defineOptions() {
    options = new SshOptions()
    options.with {

      logger = ansi()

      defaultHost = '192.168.33.144'
      defaultUser = 'vagrant'
      defaultPassword = 'vagrant'
      defaultPort = 22

      reuseConnection = true
      trustUnknownHosts = true

      verbose = true

      execOptions.with {
        showOutput = true
      }

    }
    engine = new SshDslEngine(options)
  }

}
