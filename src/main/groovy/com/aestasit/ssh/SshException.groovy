package com.aestasit.ssh

/**
 * Generic exception to be thrown in exceptional situations.
 *
 * @author Andrey Adamovich
 *
 */
class SshException extends RuntimeException {

  SshException(String message, Throwable cause) {
    super(message, cause)
  }

  SshException(String message) {
    super(message)
  }
}
