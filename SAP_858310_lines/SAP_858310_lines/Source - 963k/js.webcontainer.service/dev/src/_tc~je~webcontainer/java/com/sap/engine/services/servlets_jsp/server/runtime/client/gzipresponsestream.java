/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.runtime.client;

/*
 * Used for compressed response.
 *
 * @author Boby Kadrev
 * @version 4.0
 */

import java.io.*;
import javax.servlet.*;

import com.sap.engine.lib.io.GZIPMultiOutputStream;
import com.sap.engine.services.httpserver.interfaces.properties.HttpCompressedProperties;
import com.sap.engine.services.httpserver.lib.protocol.HeaderNames;
import com.sap.engine.services.servlets_jsp.server.WebContainerProperties;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebIOException;
import com.sap.engine.services.servlets_jsp.server.*;

/*
 * This implement compressed stream , used in GzipServletResponseWrapper.
 *
 */

public class GzipResponseStream extends ServletOutputStream {

  /**
   * The underlying gzip output stream to which we should write data.
   */
  private GZIPMultiOutputStream gzipmultistream = new GZIPMultiOutputStream();
  private ServletOutputStreamImpl servletoutput = null;
  private boolean markedClosed = false;
  private boolean closed = false;
  private int count = 0;
  private byte[] buf = null;
  private HttpServletResponseFacade response = null;
  private WebContainerProperties webContainerProperties = null;
  private HttpCompressedProperties gzipProperties = null;
  private boolean gzipStreamInitialized = false;
  private boolean bufferFlushed = false;
  private boolean gzipMode = true;
  private boolean gzipHeaderFound = false;
  private boolean flush = true;

  /**
   * Construct a servlet output stream associated with the specified Response.
   *
   * @param response The associated response
   */
  public GzipResponseStream(HttpServletResponseFacade response, ServletOutputStreamImpl output, WebContainerProperties webContainerProperties) throws IOException {
    super();
    this.webContainerProperties = webContainerProperties;
    servletoutput = output;
    this.response = response;
    buf = new byte[0];
    gzipHeaderFound = false;
  }

  public void resetInternal() {
    markedClosed = false;
    closed = false;
    count = 0;
    gzipStreamInitialized = false;
    bufferFlushed = false;
    gzipMode = true;
    gzipHeaderFound = false;
    flush = true;
  }

  public void init() {
    gzipProperties = ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getCompressedProperties();
    if (gzipProperties.getMinGZipLength() <= 0) {
      buf = new byte[0];
    } else if (buf.length != gzipProperties.getMinGZipLength()) {
      buf = new byte[gzipProperties.getMinGZipLength()];
    }
    gzipStreamInitialized = false;
    markedClosed = false;
    closed = false;
    count = 0;
    bufferFlushed = false;
    gzipMode = true;
    gzipHeaderFound = false;
    flush = true;
  }

  public boolean isWrittenAnything() {
    return count > 0;
  }

  public void setBufferSize(int bufSize) {
    servletoutput.setBufferSize(bufSize);
  }

  public void clear() throws IOException {
    if (closed) {
      throw new WebIOException(WebIOException.Attempt_to_write_after_stream_is_closed);
    }
    if (!bufferFlushed) {
      count = 0;
      return;
    }
    if (gzipMode && gzipStreamInitialized) {
      gzipmultistream.addStream(servletoutput, gzipProperties.getGZipDeflaterConstructor(), gzipProperties.getGZipCRC32Constructor());
    } else {
      servletoutput.clearBuffer();
    }
    count = 0;
  }

  /**
   * Close this output stream, causing any buffered data to be flushed.
   */
  public void close() throws IOException {
    closed = true;
    flushNotZippedBuffer();
    if (gzipMode) {
      if (count > 0) {
        ensureGzipStreamInitialized();
        gzipmultistream.close(servletoutput);
      }
    } else {
      servletoutput.close();
    }
  }

  void markClosed() {
    markedClosed = true;
  }

	public void flush(boolean force) throws IOException {
    boolean oldFlushMode = flush;
    setFlushMode(force);
    flush();
    setFlushMode(oldFlushMode);
  }
 
  /**
   * Flush any buffered data for this output stream.
   */
  public void flush() throws IOException {
    if (!flush) {
      return;
    }
    if (markedClosed) {
      if (count == 0) {
        response.removeHeader(HeaderNames.entity_header_content_encoding);
      }
    } else {
      flushNotZippedBuffer();
      if (gzipMode) {
        if (count > 0) {
          ensureGzipStreamInitialized();
          gzipmultistream.flush(servletoutput);
        }
      } else {
        servletoutput.flush();
      }
    }
  }

  /**
   * Write the specified byte to our output stream.
   *
   * @param b The byte to be written
   *
   * @throws IOException if an input/output error occurs
   */
  public void write(int b) throws IOException {
    if (closed) {
      throw new WebIOException(WebIOException.Attempt_to_write_after_stream_is_closed);
    }
    if (!bufferFlushed && count < buf.length) {
      buf[count++] = (byte) (b & 0xff);
      return;
    }
    flushNotZippedBuffer();
    if (gzipMode) {
      ensureGzipStreamInitialized();
      gzipmultistream.write(servletoutput, b);
    } else {
      servletoutput.write(b);
    }
    count++;
    // Closes stream when number of bytes sent becomes equal
    // to the content length. Servlet 2.4, SRV.5.5
    if (response.getContentLength() > 0 && response.getContentLength() == count) {
      close();
    }
  }


  /**
   * Write byte array to our output stream.
   *
   * @param b The byte array to be written
   *
   * @throws IOException if an input/output error occurs
   */
  public void write(byte b[]) throws IOException {
    write(b, 0, b.length);
  }


  /**
   * Write bytes from the specified byte array, starting
   * at the specified offset, to output stream.
   *
   * @param b   The byte array containing the bytes to be written
   * @param off Offset of the byte array
   * @param len The number of bytes to be written
   *
   * @throws IOException if an input/output error occurs
   */
  public void write(byte b[], int off, int len) throws IOException {
    if (closed) {
      throw new WebIOException(WebIOException.Attempt_to_write_after_stream_is_closed);
    }
    if (len <= 0) {
      return;
    }
    
    // Ensures number of bytes sent to be less or equal to
    // the contents length. Servlet 2.4, SRV.5.5
    int contentLength = response.getContentLength();
    if (contentLength > 0 && contentLength < count + len) {
        len = response.getContentLength() - count;
    }
    
    if (!bufferFlushed && count + len <= buf.length) {
      System.arraycopy(b, off, buf, count, len);
      count += len;
      return;
    }
    flushNotZippedBuffer();
    if (gzipMode) {
      ensureGzipStreamInitialized();
      gzipmultistream.write(servletoutput, b, off, len);
    } else {
      servletoutput.write(b, off, len);
    }
    count += len;
    // Closes stream when number of bytes sent becomes equal
    // to the content length. Servlet 2.4, SRV.5.5
    if (contentLength > 0 && contentLength == count) {
      close();
    }
  }

  public void setContentLength(int len) {

  }

  protected void setFlushMode(boolean flush) {
    this.flush = flush;
  }

  private void flushNotZippedBuffer() throws IOException {
    if (bufferFlushed) {
      return;
    }
    bufferFlushed = true;
    initWillGzip();
    if (!gzipHeaderFound && gzipProperties.getMinGZipLength() > 0 && gzipMode && closed) {
      if (count < gzipProperties.getMinGZipLength()) {
        gzipMode = false;
      }
    }
    if (gzipMode) {
      response.removeHeader(HeaderNames.entity_header_content_length);
      response.addHeaderContentEncoding("gzip");
      servletoutput.setCheckClose(false);
      servletoutput.resetChunking();
    } else {
      servletoutput.clearBuffer();
      //response.removeHeader(HeaderNames.entity_header_content_encoding);
    }
    if (count != 0) {
      if (gzipMode) {
        ensureGzipStreamInitialized();
        gzipmultistream.write(servletoutput, buf, 0, count);
      } else {
        servletoutput.write(buf, 0, count);
      }
    }
  }

  private void initWillGzip() {
    if (webContainerProperties.headerForNoCompression() != null
        && webContainerProperties.headerForNoCompression().length() > 0
        && response.containsHeader(webContainerProperties.headerForNoCompression())) {
      gzipMode = false;
    } else if (webContainerProperties.headerForCompression() != null
               && webContainerProperties.headerForCompression().length() > 0
               && response.containsHeader(webContainerProperties.headerForCompression())) {
      gzipMode = true;
      gzipHeaderFound = true;
    } else {
      gzipMode = gzipProperties.isGzip(null, response.getHeader(HeaderNames.entity_header_content_type));
    }
  }

  private void ensureGzipStreamInitialized() throws IOException {
    if (!gzipStreamInitialized) {
      gzipStreamInitialized = true;
      gzipmultistream.addStream(servletoutput, gzipProperties.getGZipDeflaterConstructor(), gzipProperties.getGZipCRC32Constructor());
    }
  }
}
