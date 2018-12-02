package com.sap.engine.services.ssl.exception;

import com.sap.tc.logging.Severity;

import java.io.*;

public class LoggerPrintStream extends PrintWriter {

  private StringBuffer buff = null;

  private static String lineSeparator = null;

  static {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintWriter writer = new PrintWriter(baos, true);
      writer.println();
      writer.close();
      lineSeparator = new String(baos.toByteArray());
    } catch (Exception e) {
      lineSeparator = new String(new byte[] { 0x13, 0x10 });
    }
  }

  public LoggerPrintStream() {
    super(System.out);
  }

  public void close() {
    super.close();
    flush();
  }

  public void flush() {
    println();
  }

  public void println() {
    if (buff != null) {
      // todo: ???????
      SSLResourceAccessor.trace(Severity.DEBUG, buff.toString());
      buff = null;
    }
  }

  public void write(int b) {
    write(String.valueOf((char) b));
  }

  public void write(byte[] buf, int off, int len) {
    write(new String(buf, off, len));
  }

  public void write(String string, int off, int len) {
    write(string.substring(off, len));
  }

  public void write(String string) {
    if (!string.equals(lineSeparator)) {
      if (buff == null) {
        buff = new StringBuffer();
      }

      buff.append(string);
    } else {
      println();
    }
  }

}
