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

package com.aestasit.infrastructure.ssh

import org.junit.Test

/**
 * ExecOptions test.
 * 
 * @author Andrey Adamovich
 *
 */
class ExecOptionsTest {

  @Test
  void testExecCtrs() {
    def defaultOpts = new ExecOptions()
    assert defaultOpts.failOnError
    assert defaultOpts.showOutput
    def opts = new ExecOptions(defaultOpts, [ failOnError: false ] )
    assert !opts.failOnError
  }

  @Test
  void testScpCtrs() {
    ScpOptions defaultOpts = new ScpOptions()
    assert defaultOpts.failOnError
    assert defaultOpts.showProgress
    ScpOptions opts = new ScpOptions(defaultOpts, [ showProgress: false ] )
    assert !opts.showProgress
  }

}
