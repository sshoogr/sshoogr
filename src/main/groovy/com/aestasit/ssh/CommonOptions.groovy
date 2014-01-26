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
 * Abstract class holding common configuration options available for EXEC and SCP functionality.
 *
 * @author Andrey Adamovich
 *
 */
abstract class CommonOptions {

  Boolean failOnError      = true
  
  def setValue(val1, dflt) {
    val1 != null ? val1 : dflt
  }

  def setValue(val2, val1, dflt) {
    setValue(val2, setValue(val1, dflt))
  }
}
