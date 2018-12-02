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
 *
 * @author Galin Galchev
 * @version 4.0
 */
import java.io.IOException;

import javax.servlet.ServletOutputStream;

import com.sap.engine.services.httpserver.interfaces.HttpParameters;
import com.sap.engine.services.httpserver.interfaces.client.Request;
import com.sap.engine.services.httpserver.interfaces.client.Response;
import com.sap.engine.services.httpserver.lib.protocol.HeaderNames;
import com.sap.engine.services.httpserver.lib.protocol.Methods;
import com.sap.engine.services.httpserver.lib.util.Ascii;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebIOException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebIllegalArgumentException;

/**
 * Provides an output stream for sending binary data to the client.
 * A ServletOutputStream object is normally retrieved via the ServletResponse.getOutputStream() method.
 *
 */
public class ServletOutputStreamImpl extends ServletOutputStream {
  private static int DEFAULT_BUFFER_SIZE = 32768;
  private static final byte[] FINAL_CHUNK = "0\r\n\r\n".getBytes();
  private static final byte[] CRLF = "\r\n".getBytes();

  /**
   * Represents the Http response for this servlet
   */
  private HttpServletResponseFacade response = null;
  /**
   * A buffer for this stream
   */
  private byte buffer[] = null;
  /**
   * Currently written bytes into a buffer
   */
  private int bufferCount = -1;
  /**
   * Total bytes written in the stream
   */
  private int totalCount = -1;
  /**
   * If the stream is closed
   */
  private boolean closed = false;
  /**
   * True if chunking is allowed.
   */
  boolean useChunking = false;
  boolean flush = true;

  /**
  * Flag that enables or disables content length check
  */
  private boolean checkClose = true;

  private Request httpRequest = null;
  private Response httpResponse = null;
  private boolean recicled = true;

  /**
   * Initiate the stream.
   *
   * @param   response  represents the Https response for this servlet
   */
  public ServletOutputStreamImpl(HttpServletResponseFacade response) {
    this.response = response;
    recicle();
  }

  /**
   * Initiate the stream.
   */
  public void init() {
    httpRequest = response.getHttpParameters().getRequest();
    httpResponse = response.getHttpParameters().getResponse();
    recicled = false;
    if (ServiceContext.getServiceContext().getWebContainerProperties().chunkResponseEnabled() && !isHead()) {
      HttpParameters httpParams = response.getHttpParameters();
      if (httpParams != null) {
        if (httpParams.getResponse().isPersistentConnection() && httpParams.getRequest().getRequestLine().getHttpMajorVersion() > 0
                && httpParams.getRequest().getRequestLine().getHttpMinorVersion() > 0) {
          // Request is HTTP/1.1 so will return chunked
          useChunking = true;
        } else {
          useChunking = false;
        }
      }
    } else {
      useChunking = false;
    }
    // check property value 
    DEFAULT_BUFFER_SIZE = ServiceContext.getServiceContext().getWebContainerProperties().getServletOutputStreamBufferSize();
    flush = true;
  }

  /**
   * Sets size of a buffer.
   *
   * @param   bufSize  size of buffer size
   */
  public void setBufferSize(int bufSize) {
    if (buffer == null || buffer.length != bufSize) {
      buffer = new byte[bufSize];
    }
  }

  /**
   * Returns a current size of the buffer.
   *
   * @return     size of the buffer
   */
  public int getBufferSize() {
    if (buffer == null) {
      return -1;
    }
    return buffer.length;
  }

  /**
   * Writes a byte into a stream.
   *
   * @param   i  a byte represented as integer
   * @exception   IOException
   */
  public void write(int i) throws IOException {
    if (LogContext.getLocationServletResponse().bePath()) {
      trace("write(int)", "i = [" + i + "], closed = [" + closed + "], i = [" + i + "]");
    }
    ensureOpen();
    totalCount++;
    if (buffer == null || buffer.length == 0) {
      commit();
      writeToClient(new byte[]{(byte)i}, 0, 1, true);
    } else {
      if (bufferCount >= buffer.length) {
        sendToClient();
      }
      buffer[bufferCount++] = (byte) i;
    }
    // Servlet specification states that if response content length is set
    // its output stream should be closed when the number of bytes sent is
    // exactly equal to the set content length
    if (checkClose && response.getContentLength() > 0
        && response.getContentLength() <= totalCount) {
      close();
    }
  }

  /**
   * Writes an array of data into a stream.
   *
   * @param   buf  array of data that will be added
   * @param   offset  offset
   * @param   length  length
   * @exception   IOException
   */
  public void write(byte buf[], int offset, int length) throws IOException {
    if (LogContext.getLocationServletResponse().bePath()) {
      String buffer = "";
      if (offset >= 0 && length >= 0 && buf.length >= offset + length) {
        buffer = new String(buf, offset, length);
      }
      trace("write(byte[])", "length = [" + length + "], closed = [" + closed + "], buf = [" + buffer + "]");
    }
    ensureOpen();
    if (offset < 0 || length < 0 || buf == null || buf.length < offset + length) {
      int bufLen = -1;
      if (buf != null) {
        bufLen = buf.length;
      }
      throw new WebIllegalArgumentException(WebIllegalArgumentException.ILLEGAL_ARGUMENTS_FOR_WRITE,
          new Object[]{buf, "" + bufLen, "" + offset, "" + length});
    }
    // Servlet specification states that if response content length is set
    // its output stream should be closed when the number of bytes sent is
    // exactly equal to the set content length
    if (checkClose && response.getContentLength() > 0
        && response.getContentLength() < totalCount + length) {
      length = response.getContentLength() - totalCount;
    }
    if (buffer == null || buffer.length == 0) {
      commit();
      writeToClient(buf, offset, length, true);
      totalCount += length;
    } else if (length > buffer.length) {
      sendToClient();
      // Bigger buffers are causing performance degradation in parallel users
      // scenarios, probably because of a synchronization problem with JStartup
      for (int off = offset, len, end = offset + length; off < end; off += len) {
        len = (end - off > buffer.length) ? buffer.length : end - off;
        writeToClient(buf, off, len, true);
      }
      totalCount += length;
    } else {
      if (length > buffer.length - bufferCount) {
        sendToClient();
      }
      System.arraycopy(buf, offset, buffer, bufferCount, length);
      bufferCount += length;
      totalCount += length;
    }
    if (checkClose && response.getContentLength() > 0
        && response.getContentLength() <= totalCount) {
      close();
    }
  }

  public void flush(boolean force) throws IOException {
    boolean oldFlushMode = flush;
    setFlushMode(force);
    flush();
    setFlushMode(oldFlushMode);
  }

  /**
   * The same like flush(), but witout flush of FCA buffers.
   * @throws IOException
   */
  private void sendToClient() throws IOException {
    commit();
    if (bufferCount > 0) {
      if (LogContext.getLocationServletResponse().bePath()) {
        trace("sendToClient()", "buffer = [" + new String(buffer, 0, bufferCount) + "]");
      }
      writeToClient(buffer, 0, bufferCount, true);
      bufferCount = 0;
    }
  }

  public void flush() throws IOException {
  	boolean bePath = LogContext.getLocationServletResponse().bePath();
    if (bePath) {
      trace("flush()", "closed = [" + closed + "]");
    }

    if (!flush) {
      return;
    }
    commit();
    if (bufferCount > 0) {
      if (bePath) {
        trace("flush()", "buffer = [" + new String(buffer, 0, bufferCount) + "]");
      }
      writeToClient(buffer, 0, bufferCount, true);
      httpResponse.flush();
      bufferCount = 0;
    }
  }

  public void finish() throws IOException {
    //ensureClientAvailable();
    if (!response.isCommitted() && httpResponse.isPersistentConnection()) {
      useChunking = false;
      if (isHead()) {
        if (response.getHeader(HeaderNames.entity_header_content_length) != null && totalCount > 0) {
          response.setIntHeader(HeaderNames.entity_header_content_length, totalCount);
        }
      } else {
        response.setIntHeader(HeaderNames.entity_header_content_length, totalCount);
      }
      if (totalCount <= 0) {
        response.removeHeader(HeaderNames.entity_header_content_encoding);
      }
    }
    flush = true;
    sendToClient();
    //response.getHttpParameters().setResponseLength(totalCount);  // This line sends totalCount to HttpAccess log
    //make it through Response interface
    response.setResponseLengthLog(totalCount);
  }

  public void setFlushMode(boolean flush) {
    this.flush = flush;
  }

  /**
   * Insert status line in header.
   */
  private void commit() throws IOException {
    if (response.isCommitted()) {
      return;
    }
    checkRecicled();
    httpRequest.getBody();
    response.commit(useChunking);
  }

  public void recicle() {
    closed = false;
    totalCount = 0;
    bufferCount = 0;
    useChunking = false;
    checkClose = true;
    DEFAULT_BUFFER_SIZE = ServiceContext.getServiceContext().getWebContainerProperties().getServletOutputStreamBufferSize();
    if (buffer == null || buffer.length != DEFAULT_BUFFER_SIZE) {
      buffer = new byte[DEFAULT_BUFFER_SIZE];
    }
    flush = true;
    recicled = true;
    httpRequest = null;
    httpResponse = null;
  }

  public void clearBuffer() {
    totalCount = 0;
    bufferCount = 0;
    checkClose = true;
  }

  /**
   * Closes the stream.
   *
   * @exception   IOException
   */
  public void close() throws IOException {
    if (LogContext.getLocationServletResponse().bePath()) {
      trace("close()", "closed = [" + closed + "]");
    }

  	boolean sendEndOfChunk = !closed;
    closed = true;
    finish();
    if (useChunking && sendEndOfChunk) {
      httpResponse.sendResponse(FINAL_CHUNK, 0, FINAL_CHUNK.length);
    }
  }

  public boolean isWritenAnything() {
    return bufferCount > 0 || response.isCommitted();
  }

  public boolean isChunked() {
    return useChunking;
  }

  private boolean isHead() {
    checkRecicled();
    byte[] command = httpRequest.getRequestLine().getMethod();
    if (command.length != Methods._HEAD.length) {
      return false;
    }
    for (int i = 0; i < Methods._HEAD.length; i++) {
      if (command[i] != Methods._HEAD[i]) {
        return false;
      }
    }
    return true;
  }

  private void ensureOpen() throws IOException {
    if (closed) {
      throw new WebIOException(WebIOException.STREAM_IS_CLOSED);
    }
    //ensureClientAvailable();
  }

//TODO - i024079 fix: check with web container developers
/*  
  private void ensureClientAvailable() throws IOException {
    checkRecicled();
    int clentId = httpRequest.getClientId();
    if (ServiceContext.getServiceContext().getConnectionsContext().containsClosedConnection(clentId)) {
      throw new WebIOException(WebIOException.CONNECTION_IS_CLOSED);
    }
  }
*/

  private void writeToClient(byte[] buf, int offset, int length, boolean isBody) throws IOException {
    checkRecicled();
    boolean bePath = LogContext.getLocationServletResponse().bePath();
    if (bePath) {
      trace("writeToClient", "i = [" + offset + "], length = [" + length + "]");
    }
    if (isBody) {
      if (isHead()) {
        return;
      }
      if (useChunking) {
        sendChunkedResponse(buf, offset, length);
      } else {
        if (bePath) {
          trace("writeToClient", "buf = [" + new String(buf, offset, length) + "]");
        }
        httpResponse.sendResponse(buf, offset, length);
      }
    } else {
      if (bePath) {
        trace("writeToClient", "buf = [" + new String(buf, offset, length) + "]");
      }
      httpResponse.sendResponse(buf, offset, length);
    }
  }

  private void checkRecicled() {
    if (recicled) {
      throw new IllegalStateException("Stream is already recycled.");
    }
  }

  private void sendChunkedResponse(byte[] chunk, int i, int j) throws IOException {
    byte[] bufferCountBytes = Ascii.hexIntToAsciiArr(j);
    byte[] newBuffer = new byte[bufferCountBytes.length + CRLF.length];
    System.arraycopy(bufferCountBytes, 0, newBuffer, 0, bufferCountBytes.length);
    System.arraycopy(CRLF, 0, newBuffer, bufferCountBytes.length, CRLF.length);
    httpResponse.sendResponse(newBuffer, 0, newBuffer.length);
    httpResponse.sendResponse(chunk, i, j);
    httpResponse.sendResponse(CRLF, 0, CRLF.length);
  }

  /**
   * @return the response on which this output stream was called
   */
  public HttpServletResponseFacade getResponse() {
    return response;
  }

  private void trace(String method, String msg) {
    LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).trace(
        "client [" + getTraceClientId() + "] ServletOutputStreamImpl." + method +
        " [" + getObjectInstance() + "] in application [" + getTraceAliasName() + "]: " + msg,
        getTraceAliasName());
  }
//TODO: 0 invocations => remove? - check with Maria; 
//  private void traceError(String method, String msg, Throwable t) {
//    LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceError(
//        "client [" + getTraceClientId() + "] ServletOutputStreamImpl." + method +
//        " [" + getObjectInstance() + "] in application [" + getTraceAliasName() + "]: " + msg +
//        ". ERROR: " + LogContext.getExceptionStackTrace(t), getTraceAliasName());
//  }

  private void traceWarning(String msgId,  String method, String msg, boolean LogContextTrace, String dcName, String csnComponent) {
    if (LogContextTrace) {
      msg = LogContext.getExceptionStackTrace(new Exception(msg));
    }
    LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning(msgId, 
        "client [" + getTraceClientId() + "] ServletOutputStreamImpl." + method +
        " [" + getObjectInstance() + "] in application [" + getTraceAliasName() + "]: " + msg,
        dcName, csnComponent);
  }

  private void traceInfo(String method, String msg) {
    LogContext.getLocationServletResponse().infoT(
        "client [" + getTraceClientId() + "] ServletOutputStreamImpl." + method +
        " [" + getObjectInstance() + "] in application [" + getTraceAliasName() + "]: " + msg);
  }

  private void tracePath(String method, String msg) {
    LogContext.getLocationServletResponse().pathT(
        "client [" + getTraceClientId() + "] ServletOutputStreamImpl." + method +
        " [" + getObjectInstance() + "] in application [" + getTraceAliasName() + "]: " + msg);
  }

  private void traceDebug(String method, String msg) {
    LogContext.getLocationServletResponse().debugT(
        "client [" + getTraceClientId() + "] ServletOutputStreamImpl." + method +
        " [" + getObjectInstance() + "] in application [" + getTraceAliasName() + "]: " + msg);
  }

  private String getObjectInstance() {
    String instance = super.toString();
    return instance.substring(instance.indexOf('@') + 1);
  }

  /**
   * Returns the client id if the requestFacade is not null, otherwise: "<NA>".
   * For tracing purposes.
   * @return
   */
  private String getTraceClientId() {
    String clientId;
    if (httpRequest != null) {
      clientId = String.valueOf(httpRequest.getClientId());
    } else {
      clientId = "<NA>";
    }
    return clientId;
  }

  /**
   * Returns the alias name if context is not null, otherwise: "<NA>".
   * For tracing purposes.
   * @return
   */
  private String getTraceAliasName() {
    if (response != null && response.getServletContext() != null) {
      return response.getServletContext().getAliasName();
    } else {
      return "<NA>";
    }
  }

  /**
   * Disables or enables chunked transfer encoding according HTTP server
   * settings, request protocol version and connection header value
   */
  public void resetChunking() {
    if (ServiceContext.getServiceContext().getWebContainerProperties()
        .chunkResponseEnabled() && !isHead()) {
      checkRecicled();
      if (httpResponse.isPersistentConnection()
          && httpRequest.getRequestLine().getHttpMajorVersion() > 0
          && httpRequest.getRequestLine().getHttpMinorVersion() > 0) {
        // Request is HTTP/1.1 so will return chunked
        useChunking = true;
      } else {
        useChunking = false;
      }
    } else {
      useChunking = false;
    }
  }

  /**
   * Enables or disables this stream to check content length
   * set by the application and auto close
   *
   * @param checkClose
   * <code>true</code> to enable check and close, otherwise <code>false</code>
   */
  protected void setCheckClose(boolean checkClose) {
    this.checkClose = checkClose;
  }
}

