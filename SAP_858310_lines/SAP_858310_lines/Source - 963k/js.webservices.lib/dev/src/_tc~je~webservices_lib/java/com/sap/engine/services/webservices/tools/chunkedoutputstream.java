/*
 * Copyright (c) 2002 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Sofia. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia.
 */
package com.sap.engine.services.webservices.tools;

import java.io.OutputStream;
import java.io.IOException;

/**
 * @author Alexander Zubev
 */
public class ChunkedOutputStream extends OutputStream {
  public static final String CRLF = "\r\n";
  public static final byte[] CRLF_BYTES = new byte[] {'\r', '\n'};
  public static final byte[] LAST_CHUNK = new byte[] {'0', '\r', '\n', '\r', '\n'};

  private OutputStream out;
  private byte[] buffer;
  private int bufferLength;
  private int pos;

  public ChunkedOutputStream(OutputStream out, int chunkSize) {
    if (out == null) {
      throw new NullPointerException("Specified OutputStream cannot be null");
    }
    if (chunkSize <= 0) {
      throw new IndexOutOfBoundsException("The size of the chunk should be positive, passed: " + chunkSize);
    }
    this.out = out;
    buffer = new byte[chunkSize];
    bufferLength = chunkSize;
    pos = 0;
  }

  private void sendChunk() throws IOException {
    byte[] chunkSize = (Integer.toHexString(pos) + CRLF).getBytes(); //$JL-I18N$
    out.write(chunkSize, 0, chunkSize.length);
    out.write(buffer, 0, pos);
    out.write(CRLF_BYTES, 0 , CRLF_BYTES.length);
    pos = 0;
    flush();
  }

  private void checkSending() throws IOException {
    if (pos == buffer.length) {
      sendChunk();
      pos = 0;
    }
  }

  public void write(byte[] b, int offset, int length) throws IOException {
    while (length > (bufferLength - pos)) {
      System.arraycopy(b, offset, buffer, pos, bufferLength - pos);
      offset += bufferLength - pos;
      length -= bufferLength - pos;
      pos += bufferLength - pos;
      sendChunk();
    }
    System.arraycopy(b, offset, buffer, pos, length);
    pos += length;
    checkSending();
  }

  public void write(byte[] b) throws IOException {
    write(b, 0, b.length);
  }

  public void write(int b) throws IOException {
    buffer[pos++] = (byte) b;
    checkSending();
  }

  public void close() throws IOException {
    if (pos > 0) {
      sendChunk();
    }
    out.write(LAST_CHUNK, 0, LAST_CHUNK.length);
    out.flush();
  } 

  public void flush() throws IOException {
    out.flush();
  }

  public static void main(String[] args) throws Exception {
    ChunkedOutputStream out = new ChunkedOutputStream(System.out, 5);
    for (int i = 0; i < 6; i++) {
      out.write('A');
    }
    out.write("AAAAAABBBBBBCCCCCC".getBytes()); //$JL-I18N$
    out.close();
  }
}
