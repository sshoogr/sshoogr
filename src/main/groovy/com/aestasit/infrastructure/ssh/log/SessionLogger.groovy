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

/**
 *
 *
 *
 */
@CompileStatic
@TypeChecked
interface SessionLogger {

  /**
   * Print information level message.
   *
   * @param message String message to print.
   */
  void info(String message)

  /**
   * Print warning level message.
   *
   * @param message String message to print.
   */
  void warn(String message)

  /**
   * Print debug level message.
   *
   * @param message String message to print.
   */
  void debug(String message)

  /**
   * Print remote command standard output stream.
   *
   * @param line String line to print.
   */
  void stdOutput(String line)

  /**
   * Print remote command standard error output stream.
   *
   * @param line String line to print.
   */
  void errOutput(String line)

  /**
   * Display a progress bar for upload/download operations.
   *
   * @param progress the actual String that represent the progress bar.
   */
  void progress(String progress)

  /**
   * Signal logger about progress bar end.
   *
   */
  void progressEnd()

}
