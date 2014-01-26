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

package com.aestasit.ssh.log

/**
 * Slf4j-based logger.
 *
 * @author Andrey Adamovich
 *
 */
class Slf4jLogger implements Logger {

  private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(getClass().getPackage().getName())

  def void info(String message) {
    logger.info(message)
  }

  def void warn(String message) {
    logger.warn(message)
  }

  def void debug(String message) {
    logger.debug(message)
  }
}
