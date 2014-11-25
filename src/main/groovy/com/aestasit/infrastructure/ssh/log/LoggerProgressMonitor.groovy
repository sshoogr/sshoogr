/*
 * Copyright (C) 2011-2014 Aestas/IT
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

/**
 * File coping progress monitor that prints progress status using logging system.
 *
 * @author Andrey Adamovich
 *
 */
class LoggerProgressMonitor implements SftpProgressMonitor {

  private final Logger logger
  private int max = 1
  private int current = 0

  LoggerProgressMonitor(Logger logger) {
    this.logger = logger
  }

  void init(int op, String src, String dest, long max) {
    this.max = max
    this.current = 0
  }

  boolean count(long count) {
    current += count
    // int percent = (current / max * 100) as int
    // TODO: logging settings
    logger.info("${current} bytes transferred")
    true
  }

  void end() {
  }
}