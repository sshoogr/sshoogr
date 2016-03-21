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

package com.aestasit.infrastructure.ssh.log

/**
 * Output stream implementation that streams all output to logging system.
 *
 * @author Andrey Adamovich
 *
 */
class LoggerOutputStream extends OutputStream implements Closeable {

  private final PipedInputStream inp
  private final PipedOutputStream out
  private final BufferedReader reader

  LoggerOutputStream(SessionLogger logger) {
    inp = new PipedInputStream()
    out = new PipedOutputStream(inp)
    reader = new BufferedReader(new InputStreamReader(inp))
    Thread.start {
      reader.eachLine { line ->
        logger.stdOutput(line)
      }
    }
  }

  void write(int chr) throws IOException {
    out.write(chr)
  }

  void write(byte[] buf, int start, int end) throws IOException {
    out.write(buf, start, end)
  }

  void flush() throws IOException {
    out.flush()
  }

  void close() throws IOException {
    out.close()
  }
}
