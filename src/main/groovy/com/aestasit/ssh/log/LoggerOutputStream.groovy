package com.aestasit.ssh.log



/**
 * Output stream implementation that streams all output to logging system. 
 *
 * @author Andrey Adamovich
 *
 */
class LoggerOutputStream extends OutputStream {

  private PipedInputStream inp
  private PipedOutputStream out
  private BufferedReader reader
  private Logger logger

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

  public void write(int chr) throws IOException {
    out.write(chr)
  }

  public void write(byte[] buf, int start, int end) throws IOException {
    out.write(buf, start, end)
  }

  public void flush() throws IOException {
    out.flush()
  }

  public void close() throws IOException {
    out.close()
  }
}
