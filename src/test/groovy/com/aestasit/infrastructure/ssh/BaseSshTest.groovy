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

import com.aestasit.ssh.mocks.MockSshServer
import org.junit.AfterClass
import org.junit.BeforeClass

/**
 * Base class for SSH functionality testing.
 * 
 * @author Andrey Adamovich
 *
 */
abstract class BaseSshTest {

  @BeforeClass
  def static void createServer() {
    MockSshServer.with {
      
      // Create command expectations.
      command('^ls.*$') { inp, out, err, callback, env ->
        out << '''total 20
drwxr-xr-x 3 1100 1100 4096 Aug  7 16:52 .
drwxr-xr-x 8 1100 1100 4096 Aug  1 17:53 ..
drwxr-xr-x 3 1100 1100 4096 Aug  7 16:49 examples
'''
        callback.onExit(0)
      }

      command('^whoami.*$') { inp, out, err, callback, env ->
        out << "root\n"
        callback.onExit(0)
      }

      command('^du.*$') { inp, out, err, callback, env ->
        out << "100\n"
        callback.onExit(0)
      }

      command('^rm.*$') { inp, out, err, callback, env ->
        out << "/tmp/test.file\n"
        callback.onExit(0)
      }

      command('^sudo.*$') { inp, out, err, callback, env ->
        out << "OK\n"
        callback.onExit(0)
      }

      command('timeout') { inp, out, err, callback, env ->
        sleep(2000)
        callback.onExit(0)
      }

      command('^touch.*$') { inp, out, err, callback, env ->
	out << "/tmp/test.file\n"
	callback.onExit(0)
      }

      command('^test -f /etc/init.conf$') { inp, out, err, callback, env ->
        callback.onExit(0)
      }

      command('^test -d /etc/init.conf$') { inp, out, err, callback, env ->
        callback.onExit(1)
      }

      command('^test -d /etc$') { inp, out, err, callback, env ->
        callback.onExit(0)
      }

      command('^test -f /etc$') { inp, out, err, callback, env ->
        callback.onExit(1)
      }

      // Create file expectations.
      dir('.')
      dir('/tmp')

      // Start server
      startSshd(2233)
      
    }
  }

  @AfterClass
  static void destroyServer() {
    MockSshServer.stopSshd()
  }

  static File getCurrentDir() {
    new File(".").getAbsoluteFile()
  }

  static File getTestFile() {
    new File("test-data/input.file").getAbsoluteFile()
  }

  static File getTestKey() {
    new File("test-data/dummy.pem").getAbsoluteFile()
  }

  static String captureOutput(Closure cl) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos)
    PrintStream old = System.out
    System.out = ps
    try {
      cl()
    } finally {
      System.out.flush()
      System.out = old
    }
    println baos.toString()
    baos.toString()
  }

  static void printThreadNames(String message) {
    println message
    Thread.allStackTraces.each { Thread t, StackTraceElement[] ste ->
      println t.name
    }
  }
}
