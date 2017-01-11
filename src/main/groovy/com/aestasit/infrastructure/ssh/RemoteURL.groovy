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

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

import java.util.regex.Pattern

@CompileStatic
@TypeChecked
@Canonical
class RemoteURL {

  public static final int USER_GROUP_INDEX = 2
  public static final int PASS_GROUP_INDEX = 4
  public static final int HOST_GROUP_INDEX = 5
  public static final int PORT_GROUP_INDEX = 7

  String host
  Integer port
  String user
  String password

  boolean passwordSet = false
  boolean portSet = false
  boolean userSet = false

  private static final Pattern URL_PATTERN = ~/^(([^:@]+)(:(.+))?@)?([^:@]+)(:(\d+))?$/

  RemoteURL(String url, int defaultPort = 22) {
    def matcher = URL_PATTERN.matcher(url)
    if (matcher.matches()) {
      host = matcher.group(HOST_GROUP_INDEX)
      if (matcher.group(PORT_GROUP_INDEX)) {
        port = matcher.group(PORT_GROUP_INDEX).toInteger()
        portSet = true
      } else {
        port = defaultPort
      }
      user = matcher.group(USER_GROUP_INDEX)
      userSet = user
      password = matcher.group(PASS_GROUP_INDEX)
      passwordSet = password
    } else {
      throw new MalformedURLException("Unknown URL format: $url")
    }
  }

}
