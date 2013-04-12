package com.aestasit.ssh.dsl

import com.aestasit.ssh.ScpOptions


/**
 * Closure delegate that is used to collect all SCP (remote file copying) options.
 *
 * @author Andrey Adamovich
 *
 */
public class ScpOptionsDelegate extends ScpOptions {

  private FileSetDelegate source = new FileSetDelegate()
  private FileSetDelegate target = new FileSetDelegate()

  def from(Closure cl) {
    cl.delegate = source
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
  }

  def into(Closure cl) {
    cl.delegate = target
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
  }
}