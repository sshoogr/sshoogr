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

import com.jcraft.jsch.SftpProgressMonitor
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * File coping progress monitor that prints progress status using logging system.
 *
 * @author Andrey Adamovich
 *
 */
@CompileStatic
@TypeChecked
class LoggerProgressMonitor implements SftpProgressMonitor {

  private final SessionLogger logger
  private long max = 1
  private long current = 0
  private boolean progressBar = true

  LoggerProgressMonitor(SessionLogger logger) {
    this.logger = logger
  }

  @SuppressWarnings('UnusedMethodParameter')
  void init(int op, String src, String dest, long max) {
    if (max != -1) {
      this.max = max
    } else {
      // unable to retrieve the file size
      // revert to print byte progress
      progressBar = false
    }
    this.current = 0
  }

  boolean count(long count) {
    current += count
    if (progressBar) {
      printProgressBar((long) ((current / max) * 100.0))
    } else {
      logger.info("${current} bytes transferred")
    }
    true
  }

  void end() {
    logger.progressEnd()
  }

  void printProgressBar(long percent) {
    long status = percent.intdiv(2).toLong()
    def bar = (0L..50L).collect {
      (it <= status) ? ((it == status) ? '>' : '=') : ' '
    }
    logger.progress("\r[${bar.join('')}] ${percent}%")
  }
}