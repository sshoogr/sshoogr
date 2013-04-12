package com.aestasit.ssh

import org.junit.Test

import com.aestasit.ssh.ExecOptions;

/**
 * ExecOptions test.
 * 
 * @author Andrey Adamovich
 *
 */
class ExecOptionsTest {

  @Test
  public void testCtrs() throws Exception {
    def defaultOpts = new ExecOptions()
    assert defaultOpts.failOnError
    def opts = new ExecOptions(defaultOpts, [ failOnError: false ] )
    assert !opts.failOnError
  }
}
