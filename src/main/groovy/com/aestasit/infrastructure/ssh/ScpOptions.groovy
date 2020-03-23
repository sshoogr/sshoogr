/*
 * Copyright (C) 2011-2020 Aestas/IT
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

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * Configuration object holding options used for SCP (remote file coping) functionality.
 *
 * @author Andrey Adamovich
 *
 */
@CompileStatic
@TypeChecked
class ScpOptions extends CommonOptions {

  Boolean showProgress     = true

  String uploadToDirectory = null
  String postUploadCommand = null // 'cp -R %from%/* %to% && rm -rf %from%'

  ScpOptions() {
  }

  ScpOptions(ScpOptions opt1) {
    this.failOnError         = setValue(opt1?.failOnError, true)
    this.showProgress        = setValue(opt1?.showProgress, true)
    this.uploadToDirectory   = setValue(opt1?.uploadToDirectory, null)
    this.postUploadCommand   = setValue(opt1?.postUploadCommand, null)
  }

  ScpOptions(ScpOptions opt1, ScpOptions opt2) {
    this(opt1, opt2?.properties)
  }

  ScpOptions(ScpOptions opt1, Map opt2) {
    this.failOnError         = setValue(opt2?.failOnError, opt1?.failOnError, true)
    this.showProgress        = setValue(opt2?.showProgress, opt1?.showProgress, true)
    this.uploadToDirectory   = setValue(opt2?.uploadToDirectory, opt1?.uploadToDirectory, null)
    this.postUploadCommand   = setValue(opt2?.postUploadCommand, opt1?.postUploadCommand, null)
  }
  
}
