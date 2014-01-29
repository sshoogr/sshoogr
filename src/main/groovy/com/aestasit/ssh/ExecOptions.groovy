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

package com.aestasit.ssh

/**
 * Configuration object holding options used for EXEC (remote command execution) functionality.
 *
 * @author Andrey Adamovich
 *
 */
class ExecOptions extends CommonOptions {

  Boolean showOutput       = true
  Boolean showCommand      = true
  Long maxWait             = 0

  Long succeedOnExitStatus = 0
  File outputFile          = null
  Boolean appendFile       = false

  String prefix            = null
  String suffix            = null
  def escapeCharacters     = null

  ExecOptions() {
  }

  ExecOptions(ExecOptions opt1) {
    this.failOnError         = setValue(opt1?.failOnError, true)
    this.showOutput          = setValue(opt1?.showOutput, true)
    this.showCommand         = setValue(opt1?.showCommand, true)
    this.maxWait             = setValue(opt1?.maxWait, 0)
    this.succeedOnExitStatus = setValue(opt1?.succeedOnExitStatus, 0)
    this.outputFile          = setValue(opt1?.outputFile, null)
    this.appendFile          = setValue(opt1?.appendFile, false)
    this.prefix              = setValue(opt1?.prefix, null)
    this.suffix              = setValue(opt1?.suffix, null)
    this.escapeCharacters    = setValue(opt1?.escapeCharacters, null)    
  }

  ExecOptions(ExecOptions opt1, ExecOptions opt2) {
    this(opt1, opt2?.properties)
  }

  ExecOptions(ExecOptions opt1, Map opt2) {
    this.failOnError         = setValue(opt2?.failOnError, opt1?.failOnError, true)
    this.showOutput          = setValue(opt2?.showOutput, opt1?.showOutput, true)
    this.showCommand         = setValue(opt2?.showCommand, opt1?.showCommand, true)
    this.maxWait             = setValue(opt2?.maxWait, opt1?.maxWait, 0)
    this.succeedOnExitStatus = setValue(opt2?.succeedOnExitStatus, opt1?.succeedOnExitStatus, 0)
    this.outputFile          = setValue(opt2?.outputFile, opt1?.outputFile, null)
    this.appendFile          = setValue(opt2?.appendFile, opt1?.appendFile, false)
    this.prefix              = setValue(opt2?.prefix, opt1?.prefix, null)
    this.suffix              = setValue(opt2?.suffix, opt1?.suffix, null)
    this.escapeCharacters    = setValue(opt2?.escapeCharacters, opt1?.escapeCharacters, null)
  }

}
