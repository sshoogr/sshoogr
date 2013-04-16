package com.aestasit.ssh

/**
 * Abstract class holding common configuration options available for EXEC and SCP functionality.
 *
 * @author Andrey Adamovich
 *
 */
abstract class CommonOptions {

  def Boolean failOnError      = true
  def Boolean verbose          = false
}
