/*
 * Copyright (c) 2002-2008 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.runtime.client;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

import com.sap.engine.services.servlets_jsp.server.exceptions.WebIOException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebIllegalArgumentException;
import com.sap.engine.services.servlets_jsp.server.lib.Constants;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.Location;

public class PrintWriterImpl extends PrintWriter {
  private final static Location CURRENT_LOCATION = LogContext.getLocationServletResponse();
  private final static String ATTEMPT_TO_WRITE_AFTER_STREAM_IS_CLOSED = "Attempt to write after the stream has been closed.";
  private final static String ERROR_WHILE_CLOSING_THE_STREAM = "Error while closing the stream.";
  private final static String CANNOT_WRITE_TO_INNER_STREAM = "Cannot write to inner stream.";
  private final static String CANNOT_FLUSH_INNER_STREAM = "Cannot flush the inner stream.";

  private OutputStream outStream = null;
  private String encoding = Constants.DEFAULT_CHAR_ENCODING;
  private int bufferSize = 0;
  private int charsWrittenBeforeFlush = 0;
  private boolean closed = false;

  public PrintWriterImpl(Writer writer) {
    super(writer);
  }

  public PrintWriterImpl(OutputStream out, int bufferSize) {
    super(out);
    this.bufferSize = bufferSize;
    this.outStream = out;
    initOut();
  }

  private void initOut() {
    OutputStreamWriter outputStreamWriter = null;

    if (encoding != null) {
      try {
        outputStreamWriter = new OutputStreamWriter(outStream, encoding);
      } catch (java.io.UnsupportedEncodingException ex) {
        outputStreamWriter = new OutputStreamWriter(outStream);
      }
    } else {
      outputStreamWriter = new OutputStreamWriter(outStream);
    }
    super.out = outputStreamWriter;
    super.lock = super.out;
  }

  public void init(String encoding, int buffSize) {
    closed = false;
    this.bufferSize = buffSize;

    if (encoding == null) {
      if (this.encoding != null) {
        this.encoding = encoding;
        //initOut();
      }
    } else if (!encoding.equals(this.encoding)) {
      this.encoding = encoding;
      //initOut();
    }
    initOut();
  }

  public void write(int c) {
    if (closed) {
    	if (CURRENT_LOCATION.beWarning()) {
    		LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000634", 
    				ATTEMPT_TO_WRITE_AFTER_STREAM_IS_CLOSED, 
        			new WebIOException(WebIOException.Attempt_to_write_after_stream_is_closed),
        			null, null);
    	}
      return;
    }
    try {
      out.write(c);
    } catch (IOException io) {
      if (CURRENT_LOCATION.beWarning()) {
        LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000619",
          CANNOT_WRITE_TO_INNER_STREAM, io, null, null);
			}
    }
    charsWrittenBeforeFlush++;
    checkFlush();
  }

  public void write(char buf[], int off, int len) {
    if (closed) {
    	if (CURRENT_LOCATION.beWarning()) {
    		LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000635", 
    				ATTEMPT_TO_WRITE_AFTER_STREAM_IS_CLOSED, 
        			new WebIOException(WebIOException.Attempt_to_write_after_stream_is_closed),
        			null, null);
    	}
      return;
    }
    if (len < 0 || len + off > buf.length || off < 0) {
      throw new WebIllegalArgumentException(WebIllegalArgumentException.Incorrect_offset_and_length);
    }
    try {
      out.write(buf, off, len);
    } catch (IOException io) {
      if (CURRENT_LOCATION.beWarning()) {
        LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000620",
          CANNOT_WRITE_TO_INNER_STREAM, io, null, null);
			}
    }
    charsWrittenBeforeFlush += len;
    checkFlush();
  }

  public void write(String s, int off, int len) {
    if (closed) {
    	if (CURRENT_LOCATION.beWarning()) {
    		LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000636", 
    				ATTEMPT_TO_WRITE_AFTER_STREAM_IS_CLOSED, 
        			new WebIOException(WebIOException.Attempt_to_write_after_stream_is_closed),
        			null, null);
    	}
      return;
    }
    if (len < 0 || len + off > s.length() || off < 0) {
      throw new WebIllegalArgumentException(WebIllegalArgumentException.Incorrect_offset_and_length);
    }
    try {
      out.write(s, off, len);
    } catch (IOException io) {
      if (CURRENT_LOCATION.beWarning()) {
        LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000621",
          CANNOT_WRITE_TO_INNER_STREAM, io, null, null);
			}
    }
    charsWrittenBeforeFlush += len;
    checkFlush();
  }

  public void println() {
    if (closed) {
    	if (CURRENT_LOCATION.beWarning()) {
    		LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000637", 
    				ATTEMPT_TO_WRITE_AFTER_STREAM_IS_CLOSED, 
        			new WebIOException(WebIOException.Attempt_to_write_after_stream_is_closed),
        			null, null);
    	}
      return;
    }
    try {
      out.write(Constants.lineSeparator);
    } catch (IOException io) {
      if (CURRENT_LOCATION.beWarning()) {
        LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000622",
          CANNOT_WRITE_TO_INNER_STREAM, io, null, null);
			}
    }
    charsWrittenBeforeFlush += Constants.lineSeparator.length();
    checkFlush();
  }

  public void close() {
    if (closed) {
      if(CURRENT_LOCATION.beDebug()) {
        CURRENT_LOCATION.traceThrowableT(Severity.DEBUG, ATTEMPT_TO_WRITE_AFTER_STREAM_IS_CLOSED, new Exception(ATTEMPT_TO_WRITE_AFTER_STREAM_IS_CLOSED));
      }
      return;
    }
    //SpecJ opt:this is not necessary, because close() method of the stream  will flush the content to FCA
    if (outStream instanceof ServletOutputStreamImpl) {
      ((ServletOutputStreamImpl)outStream).setFlushMode(false);
    }
//    } else if (outStream instanceof GzipResponseStream) {
//      CURRENT_LOCATION.errorT("$$$222 GzipResponseStream");
//      ((GzipResponseStream)outStream).setFlushMode(false);
//    }
    flush();
    try {
      outStream.close();
    } catch (IOException t) {
      if (CURRENT_LOCATION.beWarning()) {
        LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000623",
          ERROR_WHILE_CLOSING_THE_STREAM, t, null, null);
			}
    }
    closed = true;
  }

  public void flush() {
    // Must be tested...
    if (closed) {
      //      throw new IllegalStateException(SBasic.messages.getMessage("ID018308: Stream is closed!"));
    }
    try {
      out.flush();
    } catch (IOException io) {
      if (CURRENT_LOCATION.beWarning()) {
        LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000624",
          CANNOT_FLUSH_INNER_STREAM, io, null, null);
			}
    }
    charsWrittenBeforeFlush = 0;
  }

  //additional methods !
  public void reset() {
    charsWrittenBeforeFlush = 0;
    initOut();
  }

  public void resetInternal() {
    bufferSize = 0;
    charsWrittenBeforeFlush = 0;
  }

  public boolean isWrittenAnything() {
    return charsWrittenBeforeFlush != 0;
  }

  public void setBufferSize(int bufferSize) {
    if (this.bufferSize != bufferSize) {
      this.bufferSize = bufferSize;
    }
  }

  /**
   * Checks whether the buffer has to be flushed and flushes it
   */
  private void checkFlush() {
    //The container must immediately flush the contents of a filled buffer to the client.
    if (charsWrittenBeforeFlush >= bufferSize) {
      flush();
      charsWrittenBeforeFlush = 0;
    }
    //The response object is to be closed when the amount of content specified in the setContentLength method of the response has been written to the response.
    //When a response is closed, all remaining content in the response buffer must immediately flushed to the client.
    if ((outStream instanceof ServletOutputStreamImpl)) {
      int responseContentLength = ((ServletOutputStreamImpl) outStream).getResponse().getContentLength();
      if ((responseContentLength > 0) && (responseContentLength <= charsWrittenBeforeFlush)) {
        if (charsWrittenBeforeFlush > 0) {
          flush();
          charsWrittenBeforeFlush = 0;
        }
      }
    }
  }

  public boolean isClosed() {
    return closed;
  }
}
