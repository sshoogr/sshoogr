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

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

/**
 * RemoteURL parsing test.
 *
 * @author Jason Darby
 */
@RunWith(Parameterized)
class RemoteURLParsingTest {

  @Parameters
  static Collection<Object[]> data() {
    [
        ['username:password@host:5', 'username', 'password', 'host', 5] as Object[],
        ['username:p@ssword@host:5', 'username', 'p@ssword', 'host', 5] as Object[],
        ['username:password@host', 'username', 'password', 'host', 22] as Object[],
        ['username:p@ssword@host', 'username', 'p@ssword', 'host', 22] as Object[],
        ['username@host:5', 'username', null, 'host', 5] as Object[],
        ['host', null, null, 'host', 22] as Object[]
    ]
  }

  private final String url
  private final String expectedUsername
  private final String expectedPassword
  private final String expectedHost
  private final Integer expectedPort

  RemoteURLParsingTest(String url, String username, String password, String host, Integer port) {
    this.url = url
    this.expectedUsername = username
    this.expectedPassword = password
    this.expectedHost = host
    this.expectedPort = port
  }

  @Test
  void someTest() {
    new RemoteURL(url).with {
      assert user == expectedUsername
      assert password == expectedPassword
      assert host == expectedHost
      assert port == expectedPort
    }
  }

}
