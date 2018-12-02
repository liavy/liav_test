package com.sap.engine.services.servlets_jsp.server.runtime.client;

import java.io.IOException;

import javax.servlet.ServletInputStream;

/**
 * This class is used in post parameters preservation scenarios. It is wrapper of the ServletInputStream.
 * It is returned in this scenarios in ServletRequest getInputStream and getReader methods.
 * @author I044270
 *
 */
public class HttpInputStreamImplByteArray extends ServletInputStream{ 
  
  protected byte buf[];
  protected int pos;
  protected int mark = 0;
  protected int count;
  
  public HttpInputStreamImplByteArray(byte buf[]) {
      this .buf = buf;
      this .pos = 0;
      this .count = buf.length;
  }
  
  public synchronized int read() {
      return (pos < count) ? (buf[pos++] & 0xff) : -1;
  }

  public synchronized int read(byte b[], int off, int len) {
      if (b == null) {
          throw new NullPointerException();
      } else if (off < 0 || len < 0 || len > b.length - off) {
          throw new IndexOutOfBoundsException();
      }
      if (pos >= count) {
          return -1;
      }
      if (pos + len > count) {
          len = count - pos;
      }
      if (len <= 0) {
          return 0;
      }
      System.arraycopy(buf, pos, b, off, len);
      pos += len;
      return len;
  }

  public synchronized long skip(long n) {
      if (pos + n > count) {
          n = count - pos;
      }
      if (n < 0) {
          return 0;
      }
      pos += n;
      return n;
  }

  public synchronized int available() {
      return count - pos;
  }

  public boolean markSupported() {
      return true;
  }

  public void mark(int readAheadLimit) {
      mark = pos;
  }

  /**
   * Resets the buffer to the marked position.  The marked position
   * is 0 unless another position was marked or an offset was specified
   * in the constructor.
   */
  public synchronized void reset() {
      pos = mark;
  }

  public void close() throws IOException {
  }


}
