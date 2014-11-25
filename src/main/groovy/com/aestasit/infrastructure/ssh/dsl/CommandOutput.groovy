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

package com.aestasit.infrastructure.ssh.dsl

/**
 * This class holds data for remote command execution result.
 *
 * @author Andrey Adamovich
 *
 */
class CommandOutput {

  int exitStatus
  String output
  Throwable exception

  CommandOutput(int exitStatus, String output) {
    this.exitStatus = exitStatus
    this.output = output
  }

  CommandOutput(int exitStatus, String output, Throwable exception) {
    this.exitStatus = exitStatus
    this.output = output
    this.exception = exception
  }

  /**
   * Convenience method to verify that the
   * command output has failed.
   *
   * @return true if the command has failed
   */
  boolean failed() {
    this.exitStatus != 0
  }

}