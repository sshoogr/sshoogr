package com.aestasit.infrastructure.ssh.dsl

/**
 * Command output parsing utilities.
 *
 * @author Andrey Adamovich
 */
class ParsingUtils {

  /**
   * Get single integer from the command output.
   *
   * @param out command output object.
   * @return parsed integer or null.
   */
  static Integer resolveId(CommandOutput out) {
    if (out.output.isInteger()) {
      return out.output.toInteger()
    }
    null
  }

}
