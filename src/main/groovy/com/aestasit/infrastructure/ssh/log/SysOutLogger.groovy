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

/**
 * Standard system output logger.
 *
 * @author Andrey Adamovich
 *
 */
class SysOutLogger implements Logger {

  def void info(String message) {
    println "$message"
  }

  def void warn(String message) {
    println "WARN: $message"
  }

  def void debug(String message) {
    println "DEBUG: $message"
  }
}
