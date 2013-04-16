package com.aestasit.ssh.log



/**
 * Output stream implementation that streams all output to logging system.
 *
 * @author Andrey Adamovich
 *
 */
class LoggerOutputStream extends OutputStream {

  private final PipedInputStream inp
  private final PipedOutputStream out
  private final BufferedReader reader

  LoggerOutputStream(Logger logger) {
    inp = new PipedInputStream()
    out = new PipedOutputStream(inp)
    reader = new BufferedReader(new InputStreamReader(inp))
    Thread.start {
      reader.eachLine { line ->
        logger.info(line)
      }
    }
  }

  def void write(int chr) throws IOException {
    out.write(chr)
  }

  def void write(byte[] buf, int start, int end) throws IOException {
    out.write(buf, start, end)
  }

  def void flush() throws IOException {
    out.flush()
  }

  def void close() throws IOException {
    out.close()
  }
}
