/*
 * Copyright (c) 2002 by SAP Labs Sofia,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Sofia. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia.
 */
package com.sap.engine.services.webservices.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.sap.tc.logging.Location;


public class ChunkedInputStream extends InputStream {
  private boolean streamClosed = false;
  private boolean bof = true;
  private boolean eof = false;
  private InputStream inputStream;
  private int chunkSize;
  private int curPos;
  
  private static String ASCII_CHARSET = "US-ASCII";
  private static Location LOCATION = Location.getLocation(ChunkedInputStream.class);
  

  public ChunkedInputStream(final InputStream inputStream){
    if (inputStream == null) {
      throw new IllegalArgumentException("InputStream is null.");
    }
    this.inputStream = inputStream;
    this.curPos = 0;
  }

  public int read() throws IOException {  
    if (streamClosed) {
      throw new IOException("Cannot read - stream already closed.");
    }
    if (eof) {
      return -1;
    } 
    
    if (curPos >= chunkSize) {
      nextChunk();
      if (eof) { 
        return -1;
      }
    }
    ++curPos;
    return inputStream.read();
  }

  public int read (byte[] b, int off, int len) throws IOException {
    if (streamClosed) {
      throw new IOException("Cannot read - stream already closed.");
    }

    if (eof) { 
      return -1;
    }
    if (curPos >= chunkSize) {
      nextChunk();
      if (eof) { 
        return -1;
      }
    }
    len = Math.min(len, chunkSize - curPos);
    int count = inputStream.read(b, off, len);
    curPos += count;
    return count;
  }

  public int read (byte[] b) throws IOException {
    return read(b, 0, b.length);
  }
  
  private void nextChunk() throws IOException {
    if (!bof) {
      int cr = inputStream.read();
      int lf = inputStream.read();
      if ((cr != '\r') || (lf != '\n')) { 
        throw new IOException("No CRLF at the end of the chunk: [" + cr + "/" + lf + "]");
      }
    }
    chunkSize = readChunkSize(inputStream);
    bof = false;
    curPos = 0;
    if (chunkSize == 0) {
      eof = true;
      readLine();
    }
  }

  private static int readChunkSize(final InputStream in) 
  throws IOException {

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    // States: 0=normal, 1=\r was scanned, 2=inside quoted string, -1=end
    int state = 0; 
    while (state != -1) {
      int b = in.read();
      if (b == -1) { 
        throw new IOException("Stream ended unexpectedly");
      }
      switch (state) {
      case 0: 
        switch (b) {
        case '\r':
          state = 1;
          break;
        case '\"':
          state = 2;
          /* fall through */
        default:
          baos.write(b);
        }
        break;

      case 1:
        if (b == '\n') {
          state = -1;
        } else {
          // this was not CRLF
          throw new IOException("Unexpected single newline character in chunk size");
        }
        break;

      case 2:
        switch (b) {
        case '\\':
          b = in.read();
          baos.write(b);
          break;
        case '\"':
          state = 0;
          /* fall through */
        default:
          baos.write(b);
        }
        break;
      default: throw new RuntimeException("Unknown state");
      }
    }

    byte[] buf = baos.toByteArray();
    String chunkSizeStr;
    try {
      chunkSizeStr =  new String(buf, 0, buf.length, ASCII_CHARSET);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("No support for ASCII encoding");
    }
    
    
    int separatorPos = chunkSizeStr.indexOf(';');
    chunkSizeStr = (separatorPos > 0)
    ? chunkSizeStr.substring(0, separatorPos).trim() : chunkSizeStr.trim();

    int result;
    try {
      result = Integer.parseInt(chunkSizeStr.trim(), 16);
    } catch (NumberFormatException e) {
      throw new IOException ("Invalid chunk size: " + chunkSizeStr);
    }
    if (LOCATION.beDebug()){
      LOCATION.debugT("Chunk size read from stream: [" + result + "]");
    }
    return result;
  }

  public void close() throws IOException {
    if (!streamClosed) {
      try {
        if (!eof) {
          byte buffer[] = new byte[1024];
          while (read(buffer) >= 0) {
            ;
          }
        }
      } finally {
        eof = true;
        streamClosed = true;
      }
    }
  }
  
  public String readLine() throws IOException {
    int b;
    StringBuffer line = new StringBuffer();

    while ((b = inputStream.read()) != -1) {
      if (b != '\r' && b != '\n') {
        line.append((char) b);
      } else {
        if (b == '\r') {
          int b2 = inputStream.read();

          if (b2 != '\n') {
            throw new IOException("CR found without LF. CRLF should be used for a line termination!");
          }
        }

        return line.toString();
      }
    }

    return "";
  }
  
}



