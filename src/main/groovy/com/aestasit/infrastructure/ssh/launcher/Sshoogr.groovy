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

package com.aestasit.infrastructure.ssh.launcher

import com.aestasit.infrastructure.ssh.DefaultSsh
import com.aestasit.infrastructure.ssh.log.AnsiLogger
import com.aestasit.infrastructure.ssh.log.Slf4jLogger
import com.aestasit.infrastructure.ssh.log.SysOutLogger
import com.lexicalscope.jewel.cli.CliFactory
import com.lexicalscope.jewel.cli.HelpRequestedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

/**
 * Sshoogr script launcher application.
 *
 * @author Andrey Adamovich
 *
 */
final class Sshoogr {

  static void main(String[] args) {
    try {
      final SshoogrOptions options = CliFactory.parseArguments(SshoogrOptions, args)
      options.inputFiles.each { File inputFile ->
        if (inputFile.exists()) {
          GroovyShell shell = new GroovyShell(buildBinding(options), compilerConfiguration())
          Script script = shell.parse(inputFile)
          script.invokeMethod('init', null)
          script.run()
        } else {
          System.err.println "File not found: ${inputFile.absolutePath}\n"
          System.err.println "Sshoogr requires an input file to work!\n"
          System.err.println "See https://github.com/aestasit/sshoogr for details."
          System.exit(127)
        }
      }
    } catch (HelpRequestedException e) {
      println e.message
    } catch (Exception ex) {
      System.err.println "${ex.message}"
      System.exit(1)
    }
  }

  private static Binding buildBinding(SshoogrOptions options) {
    new Binding(init: {
      switch (options.logger) {
        case 'slf4j':
          DefaultSsh.logger = new Slf4jLogger()
          break
        case 'standard':
          DefaultSsh.logger = new SysOutLogger()
          break
        case 'color':
          DefaultSsh.logger = new AnsiLogger()
          break
        default:
          throw new RuntimeException("Unknown logger type!")
      }
      DefaultSsh.defaultHost = options.host
      DefaultSsh.defaultUser = options.user
      DefaultSsh.defaultPort = options.port
      DefaultSsh.defaultKeyFile = options.key
      DefaultSsh.defaultPassword = options.password
      DefaultSsh.trustUnknownHosts = options.isTrust()
    })
  }

  private static CompilerConfiguration compilerConfiguration() {
    new CompilerConfiguration().
      addCompilationCustomizers(
        new ImportCustomizer()
          .addStaticStars(DefaultSsh.name)
      )
  }

}
