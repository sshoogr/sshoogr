package com.aestasit.ssh

/**
 * Generic exception to be thrown in exceptional situations.
 *
 * @author Andrey Adamovich
 *
 */
class SshException extends RuntimeException {

  public SshException(String message, Throwable cause) {
    super(message, cause)
  }

  public SshException(String message) {
    super(message)
  }
}
