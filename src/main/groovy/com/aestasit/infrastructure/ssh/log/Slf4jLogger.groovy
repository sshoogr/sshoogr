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

package com.aestasit.infrastructure.ssh.log

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.slf4j.LoggerFactory

/**
 * Slf4j-based logger.
 *
 * @author Andrey Adamovich
 *
 */
@TypeChecked
@CompileStatic
class Slf4jLogger implements Logger {

  static private final org.slf4j.Logger LOG = LoggerFactory.getLogger(Slf4jLogger)

  void info(String message) {
    LOG.info(message)
  }

  void warn(String message) {
    LOG.warn(message)
  }

  void debug(String message) {
    LOG.debug(message)
  }

  void stdOutput(String line) {
    LOG.debug(line)
  }

  void errOutput(String line) {
    LOG.debug(line)
  }

  void progress(String progress) {
    LOG.trace(progress)
  }

  void progressEnd() {
    // do nothing
  }

}
