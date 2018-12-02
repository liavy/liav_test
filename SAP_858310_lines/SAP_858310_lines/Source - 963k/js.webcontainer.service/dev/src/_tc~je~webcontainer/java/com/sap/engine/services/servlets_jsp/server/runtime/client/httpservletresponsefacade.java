/*
 * Copyright (c) 2000-2008 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.runtime.client;

import static com.sap.engine.services.servlets_jsp.server.ServiceContext.getServiceContext;

import com.sap.engine.services.httpserver.interfaces.ErrorData;
import com.sap.engine.services.httpserver.interfaces.ErrorPageTemplate;
import com.sap.engine.services.httpserver.interfaces.SupportabilityData;
import com.sap.engine.services.httpserver.interfaces.io.HttpInputStream;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.httpserver.lib.ProtocolParser;
import com.sap.engine.services.httpserver.lib.ResponseCodes;
import com.sap.engine.services.httpserver.lib.Responses;
import com.sap.engine.services.httpserver.lib.headers.MimeHeaders;
import com.sap.engine.services.httpserver.lib.protocol.HeaderNames;
import com.sap.engine.services.httpserver.lib.protocol.HeaderValues;
import com.sap.engine.services.httpserver.lib.util.ByteArrayUtils;
import com.sap.engine.services.httpserver.server.errorreport.ErrorReportInfoBean;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebIOException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebIllegalArgumentException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebIllegalStateException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebMalformedURLException;
import com.sap.engine.services.servlets_jsp.server.lib.Constants;
import com.sap.engine.session.Session;
import com.sap.tc.logging.Location;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Extends the ServletResponse interface to provide HTTP-specific functionality
 * in sending a response. For example, it has methods to access HTTP headers and cookies.
 * The servlet container creates an HttpServletRequest object and passes it
 * as an argument to the servlet's service methods (doGet, doPost, etc).
 *
 * @author Galin Galchev
 * @version 4.0
 */
public class HttpServletResponseFacade extends HttpServletResponseBase {
  private static Location currentLocation = Location.getLocation(HttpServletResponseFacade.class);
  private static Location traceLocation = LogContext.getLocationServletResponse();
  /**
   * The output stream containing the response from the servlet
   */
  private ServletOutputStreamImpl output;

  private GzipResponseStream gzipResponseStream = null;
  /**
   * Output stream containing response of the servlet as PrintWriter
   */
  private PrintWriterImpl printwriter;

  private PrintWriterImpl gzipprintwriter;

  private JspWriterToPrintWriterWrapper jspWriterToPrintWriterWrapper = null;
  /**
   * Whether the output stream is taken as OutputStrem object
   */
  private boolean getOutputStream = false;

  private boolean markWriter = false;//TODO: never set to true; method markWriter() is empty
  private String servletName = null;
  private boolean isClosed = false;
  private boolean isGZip = false;

  private boolean modifiable = true;

  /**
   * Initiates the instance without parameters
   */
  public HttpServletResponseFacade() throws IOException {
    if (traceLocation.beDebug()) {
      traceDebug("HttpServletResponseFacade", "new response is created");
    }
    output = new ServletOutputStreamImpl(this);
    printwriter = new PrintWriterImpl(output, output.getBufferSize());
    gzipResponseStream = new GzipResponseStream(this, output, serviceContext.getWebContainerProperties());
    gzipprintwriter = new PrintWriterImpl(gzipResponseStream, output.getBufferSize());
    jspWriterToPrintWriterWrapper = new JspWriterToPrintWriterWrapper(printwriter);
    resetInternal(true);
  }

  /**
   * Initiates the instance with the original Http response.
   *
   * @param responseHeaders Http response headers
   */
  public void init(ApplicationContext applicationContext, HttpServletRequestFacade requestFacade, MimeHeaders responseHeaders) {
    if (traceLocation.beDebug()) {
      traceDebug("init", "init is called");
    }
    super.init(applicationContext, responseHeaders, requestFacade);
    contentLength = -1;
    //setStatusLocked(false);//TODO: Reverted in Servlet 2.5
    setStatus(ResponseCodes.code_ok);
    if (!ServiceContext.getServiceContext().getWebContainerProperties().isSuppressXPoweredBy()) {
      setHeader(HeaderNames.response_header_x_powered_by, HeaderValues.x_powered_by);
    }
    if (ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getUseServerHeader()) {
      setHeader(HeaderNames.response_header_server_, serviceContext.getServerVersion());
    }
    output.init();
    if (traceLocation.beDebug()) {
      traceDebug("init", "new response initialized");
    }
  }

  public void reset() {
    reset(false);
    if (traceLocation.beDebug()) {
      trace("reset", "isIncluded = [" + isIncluded + "].");
    }
  }

  protected void reset(boolean keepHeaders) {
    if (locked(true)) {
      if (traceLocation.beWarning()) {
        LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000544",
            "client [{0}] HttpServletResponseFacade.reset" +
            " [{1}] in application [{2}]: {3}", new Object[]{getTraceClientId(), getObjectInstance(), getTraceAliasName(), LogContext.getExceptionStackTrace(new Exception("isCommitted true - throwing exception"))}, null, null);

      }
      return;
    }
    super.reset(keepHeaders);
    contentLength = -1;
    if (keepHeaders) {
      //removeHeader(HeaderNames.entity_header_content_encoding);
    }
    if (getWriter) {
      if (isGZip) {
        gzipprintwriter.reset();
      } else {
        printwriter.reset();
      }
    }

    output.clearBuffer();
    if (isGZip) {
      try {
        gzipResponseStream.clear();
        //addHeader(HeaderNames.entity_header_content_encoding_, gzip);
      } catch (IOException t) {
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000196",
          "Can't clear the gzip stream.", t, null, null);
      }
    }
    //setStatusLocked(false); //unlock the setStatus method //TODO: Reverted in Servlet 2.5
    setStatus(ResponseCodes.code_ok);
    if (ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getUseServerHeader()) {
      setHeader(HeaderNames.response_header_server_, serviceContext.getServerVersion());
    }
    if (!ServiceContext.getServiceContext().getWebContainerProperties().isSuppressXPoweredBy()) {
      setHeader(HeaderNames.response_header_x_powered_by, HeaderValues.x_powered_by);
    }
  }

  /**
   * Clear this object, ready to  return it in ObjectPool
   */
  public void resetInternal(boolean initial) throws IOException {
    if (traceLocation.beDebug()) {
      traceDebug("resetInternal", "response cleared");
    }
    super.resetInternal(initial);
    getOutputStream = false;
    getWriter = false;
    markWriter = false;
    isCharacterEncodingSet = false;
    jspWriterToPrintWriterWrapper.reset();
    printwriter.resetInternal();
    gzipprintwriter.resetInternal();
    gzipResponseStream.resetInternal();
    output.recicle();
    servletName = null;
    isClosed = false;
    isGZip = false;
    contentLength = -1;
  }

  public void setGZip() {
    isGZip = true;
    //addHeader(HeaderNames.entity_header_content_encoding_, gzip);
    gzipResponseStream.init();
    if (traceLocation.bePath()) {
      tracePath("setGZip", "will use gzip stream");
    }
  }

  public boolean isGZip() {
    return isGZip;
  }

  /**
   * Encodes the specified URL by including the session ID in it, or,
   * if encoding is not needed, returns the URL unchanged.
   *
   * @param s some URL
   * @return encoded URL containing session information
   * @deprecated
   */
  public String encodeUrl(String s) {
    return encodeURL(s);
  }

  /**
   * Returns a ServletOutputStream suitable for writing binary data in the response.
   *
   * @return Output Stream of the response
   * @throws IOException
   */
  public ServletOutputStream getOutputStream() throws IOException {
	if (traceLocation.beDebug()) {
	          LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceDebug("Entering method HttpServletResponseFacade.getOutputStream" +
	          		" with client ["+getTraceClientId()+"], in application ["+getTraceAliasName()+"].", getTraceAliasName() );
	}
    if (getWriter || markWriter) {
      if (traceLocation.beWarning()) {
        LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000545",
            "client [{0}] HttpServletResponseFacade.getOutputStream" +
            " [{1}] in application [{2}]: {3}", new Object[]{getTraceClientId(), getObjectInstance(), getTraceAliasName(),  LogContext.getExceptionStackTrace(new Exception("getWriter == true, throwing exception"))}, getServletContext().getApplicationName(), getServletContext().getCsnComponent());
      }
      throw new WebIllegalStateException(WebIllegalStateException.Stream_is_already_taken_with_method, new Object[]{"getWriter()"});
    }
    getOutputStream = true;
    if (isGZip) {
      return gzipResponseStream;
    } else {
      return output;
    }
  }

  /**
   * The same as getOutputStream but without some checks. Used only from container.
   *
   * @return Output Stream of the response
   */
  public ServletOutputStream getOutStream() {
    if (isGZip) {
      return gzipResponseStream;
    }
    return output;
  }

  public void markWriter() {
    //TODO:Added empty with changelist 81744:"The jsp writer must be initialized on flushing the output, not on initializing the jsp. Otherwise the encoding may be lost."
  }

  /**
   * Returns a PrintWriter object that can send character text to the client.
   *
   * @return PrintWriter object
   */
  public PrintWriter getWriter() {
	boolean beDebug = traceLocation.beDebug();
    if (beDebug) {
          LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceDebug("Entering method HttpServletResponseFacade.getWriter" +
          		" with client ["+getTraceClientId()+"], in application ["+getTraceAliasName()+"].", getTraceAliasName() );
    }
    if (getOutputStream) {
      if (traceLocation.beWarning()) {
        LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000546",
            "client [{0}] HttpServletResponseFacade.getWriter" +
            " [{1}] in application [{2}]: {3}", new Object[]{getTraceClientId(), getObjectInstance(), getTraceAliasName(), LogContext.getExceptionStackTrace(new Exception("getOutputStream == true, throwing exception"))}, getServletContext().getApplicationName(), getServletContext().getCsnComponent());

      }
      throw new WebIllegalStateException(WebIllegalStateException.Stream_is_already_taken_with_method, new Object[]{"getOutputStream()"});
    }
    if (!getWriter) {
      try {
        if (!isCharacterEncodingSet) {
          //If the response's character encoding has not
          //been specified as described in getCharacterEncoding (i.e., the method just
          //returns the default value ISO-8859-1), getWriter updates it to ISO-8859-1.
          if (characterEncoding == null) {
            updateCharacterEncoding(Constants.DEFAULT_CHAR_ENCODING);
            if (beDebug) {
              traceDebug("getWriter", "The response's character encoding has not been specified. getWriter updates it to [" + Constants.DEFAULT_CHAR_ENCODING + "]");
            }
          } else {
            updateCharacterEncoding(characterEncoding);
            if (beDebug) {
              traceDebug("getWriter", "The response's character encoding has not been specified. getWriter updates it to [" + characterEncoding + "]");
            }
          }
        }
      } catch (Throwable ex) {
        if (beDebug) {
          traceDebug("getWriter", "The response's character encoding has not been specified. getWriter cannot update it.", ex);
        }
      }
      getWriter = true;
      if (isGZip) {
        gzipprintwriter.init(getCharacterEncoding(), getBufferSize());
        if (isClosed) {
          gzipprintwriter.close();
        }
      } else {
        printwriter.init(getCharacterEncoding(), getBufferSize());
        if (isClosed) {
          printwriter.close();
        }
      }
    }
    String includeInOut = (String) getServletRequest().getAttribute("com.sap.engine.internal.jsp.includeInOut");
    if (includeInOut != null && includeInOut.equals("true")) {
      jspWriterToPrintWriterWrapper.init((JspWriter) getServletRequest().getAttribute("com.sap.engine.internal.jsp.out"));
      return jspWriterToPrintWriterWrapper;
    }
    if (isGZip) {
      return gzipprintwriter;
    } else {
      return printwriter;
    }
  }

  /**
   * Sends an error response to the client using the specified status.
   * Internal Usage: Use this method ONLY if we do not expect user action (End-User Problem Reporting feature)
   *
   * @param i ID of the status
   * @throws IOException
   */
  public void sendError(int i) throws IOException {
    sendError(new ErrorData(i, ResponseCodes.reason(i, getServletContext().getAliasName()),
      "", false, new SupportabilityData()), null);
  }

  /**
   * Sends an error response to the client using the specified
   * status code and descriptive message.
   * Internal Usage: Use this method ONLY if we do not expect user action (End-User Problem Reporting feature)
   *
   * @param i ID of the status
   * @param s Explanation of the status
   * @throws IOException
   */
  public void sendError(int i, String s) throws IOException {
    sendError(new ErrorData(i, s, "", false, new SupportabilityData()), null);
  }

  /**
   * Sends an error response to the client using the specified status code and message.
   * This is the same as HttpServletResponse.sendError(int i, String s) except that
   * the Web Container can be instructed whether to escape scripts that can be placed
   * into the error response message or not. When used with checkHtml = false,
   * the Web Container does not escape scripts.
   * Internal Usage: Use this method ONLY if we do not expect user action (End-User Problem Reporting feature)
   *
   * @param i ID of the status
   * @param s Explanation of the status
   * @param checkHtml if true, will instruct Web Container to escape scripts found in the
   * error message
   * @throws IOException
   */
  public void sendError(int i, String s, boolean checkHtml) throws IOException {
    sendError(new ErrorData(i, s, "", !checkHtml, new SupportabilityData()), null);
  }

  /**
   * Sends an error response to the client using the specified status code, message
   * and additional details.
   * This is the same as HttpServletResponse.sendError(int i, String s) except that
   * the Web Container can be instructed whether to escape scripts that can be placed
   * into the error response message or not. When used with checkHtml = false,
   * the Web Container does not escape scripts.
   * Internal Usage: Use this method ONLY if we do not expect user action (End-User Problem Reporting feature)
   *
   * @param i the error status code
   * @param s the descriptive message
   * @param details more details regarding the error situation to be placed in the error response message
   * @param checkHtml if true, will instruct Web Container to escape scripts found in the
   * error message
   * @throws IOException If an input or output exception occurs
   */
  public void sendError(int i, String s, String details, boolean checkHtml) throws IOException {
    sendError(new ErrorData(i, s, details, !checkHtml, new SupportabilityData()), null);
  }

  /**
   * Sends an error response to the client using the specified error data.
   * The Web Container can be instructed whether to escape scripts that can be replaced
   * into the error response message or not.
   *
   * @param errorData the error data.
   * @param errorPageTemplate specify the error page template, if it is <code>null</code>
   * then the error page template provided by the Web Container will be used.
   * @throws IOException if an input or output exception occurs.
   */
  public void sendError(ErrorData errorData, ErrorPageTemplate errorPageTemplate) throws IOException {
    if (errorData == null) {
      if (traceLocation.beDebug()) {
        traceDebug("sendError", "errorData=null");
      }
      return;
    }
    
    int i = errorData.getErrorCode();
    if (i == -1) {
      i = ResponseCodes.code_internal_server_error;
    }
    
    String s = errorData.getMessage();
    String details = errorData.getAdditionalMessage();
    getServletRequest().setAttribute(Constants.ERROR_PAGE_TEMPLATE, errorPageTemplate);
    
    if (locked(true)) {
      if (traceLocation.beWarning()) {
        LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000547",
          "client [{0}] HttpServletResponseFacade.sendError" +
          " [{1}] in application [{2}]: code = [{3}], message = [{4}], details = [{5}]: isCommitted || isCommittedError, throwing exception",
          new Object[]{getTraceClientId(), getObjectInstance(), getTraceAliasName(), i, s, details}, getServletContext().getApplicationName(), getServletContext().getCsnComponent());
      }
      return;
    } else {
      if (traceLocation.bePath()) {
        trace("sendError", "code = [" + i + "], message = [" + s + "], details = [" + details + "], isIncluded = [" + isIncluded + "]");
      }
    }

    // If message is null the default error message is used
    if (s == null) {
      s = ResponseCodes.reason(i, getServletContext().getAliasName());
    }

    clearBuffer();

    setStatus(i);

    HttpInputStream requestBody = null;
    try {
      InputStream requestInput = getServletRequest().getInputStreamNoCheck();
      if (requestInput instanceof HttpInputStream) {
        requestBody = (HttpInputStream) requestInput;
      }
    } catch (ThreadDeath t) {
      throw t;
    } catch (OutOfMemoryError t) {
      throw t;
    } catch (Throwable t) {
      // TODO:Polly type:ok
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(currentLocation, "ASJ.web.000124",
        "Cannot get input stream for reading request body.", t, null, null);
    }
    if (requestBody != null && !requestBody.isEmpty()) {
      getServletRequest().getHttpParameters().getResponse().setPersistentConnection(false);
    }

    getServletRequest().getHttpParameters().setErrorData(errorData);

    try {
      if (!doError(i, s, "sendError")) {
        setHeader("Pragma", "no-cache");
        setContentType("text/html;charset=ISO-8859-1");
        
        String errorBody = " ";
        
        if (isGZip) {
          if (getWriter) {
            gzipprintwriter.println(errorBody);
          } else {
            getOutputStream().println(errorBody);
          }
          finish();
        } else {
          setContentLength(errorBody.length());
          sendBodyText(errorBody);
        }
      }
      
      close();    
    } catch (ServletException se) {
      if (traceLocation.beError()) {
        LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceError("ASJ.web.000438",
          "client [{0}] HttpServletResponseFacade.sendError [{1}] in application [{2}]: error occurred in invoking error page. ERROR: {3}",
          new Object[]{getTraceClientId(), getObjectInstance(), getTraceAliasName(), LogContext.getExceptionStackTrace(se)}, null, null);
      }
      throw new WebIOException(WebIOException.Error_invoking_the_error_page, se);
    }
  }

  public void writeError(ErrorData errorData) throws IOException {
    int i = errorData.getErrorCode();
    if (i == -1) {
      i = ResponseCodes.code_internal_server_error;
    }

    String s = errorData.getMessage();
    String details = errorData.getAdditionalMessage();

    if (traceLocation.bePath()) {
      tracePath("writeError", "code = [" + i + "], message = [" + s + "], details = [" + details + "]");
    }

    getServletRequest().getHttpParameters().getResponse().setPersistentConnection(false);

    getServletRequest().getHttpParameters().setErrorData(errorData);

    try {
      if (!doError(i, s, "writeError")) {
        String errorBody = " ";
        
        if (isGZip) {
          if (getWriter) {
            gzipprintwriter.println(errorBody);
          } else {
            getOutputStream().println(errorBody);
          }
          finish();
        } else {
          sendBodyText(errorBody);
        }
      }
      
      close();
    } catch (ServletException se) {
      if (traceLocation.beError()) {
        LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceError("ASJ.web.000668",
          "client [{0}] HttpServletResponseFacade.writeError [{1}] in application [{2}]: error occurred in invoking error page. ERROR: {3}",
          new Object[]{getTraceClientId(), getObjectInstance(), getTraceAliasName(), LogContext.getExceptionStackTrace(se)}, null, null);
      }
      throw new WebIOException(WebIOException.Error_invoking_the_error_page, se);
    }
  }
  
  private void sendBodyText(String s) throws IOException {
    if (isGZip) {
      gzipResponseStream.print(s);
    } else {
      output.print(s);
    }
  }

  /**
   * Closes the OutputStream of the servlet.
   *
   * @throws IOException
   */
  public void close() throws IOException {
    isClosed = true;
    if (getWriter) {
      if (isGZip) {
        gzipprintwriter.close();
      } else {
        printwriter.close();
      }
      return;
    }
    if (isGZip) {
      gzipResponseStream.close();
    }
    output.close();
  }

  /**
   * Sends a temporary redirect response to the client using the specified
   * redirect location URL.
   *
   * @param s URL where the request will be redirected
   * @throws IOException
   * @throws IllegalArgumentException
   */
  public void sendRedirect(String s) throws IOException, java.lang.IllegalArgumentException {
    if (locked(true)) {
      if (traceLocation.beWarning()) {
        String msg = LogContext.getExceptionStackTrace(new Exception("url = [" + s + "], isIncluded = [" + isIncluded + "], isCommitted: throwing exception"));
        LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000548",
            "client [{0}] HttpServletResponseFacade.sendRedirect" +
            " [{1}] in application [{2}]: {3}", new Object[]{getTraceClientId(),  getObjectInstance(), getTraceAliasName(), msg}, getServletContext().getApplicationName(), getServletContext().getCsnComponent());

      }
      return;
    } else {
      if (traceLocation.bePath()) {
        tracePath("sendRedirect", "url = [" + s + "], isIncluded = [" + isIncluded + "]");
      }
    }
    if (s == null) {
      throw new WebIllegalArgumentException(WebIllegalArgumentException.Cannot_redirect_to_null_location);
    } else {
      reset(true);
      setStatus(ResponseCodes.code_found);
      setContentType("text/html");
      try {
        s = makeAbsolute(s);
      } catch (MalformedURLException me) {
        if (traceLocation.beError()) {
          LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceError("ASJ.web.000439",
              "client [{0}] HttpServletResponseFacade.sendRedirect" +
              " [{1}] in application [{2}]: cannot convert url" +
              ". ERROR: {3}", new Object[]{getTraceClientId(), getObjectInstance(), getTraceAliasName(), LogContext.getExceptionStackTrace(me)}, getServletContext().getApplicationName(), getServletContext().getCsnComponent());
        }
        throw new WebIllegalArgumentException(WebIllegalArgumentException.Cannot_be_converted_into_URL, new Object[]{s}, me);
      }
      setHeader("Location", s);
      byte[] body = Responses.generate302FoundBody(serviceContext
        .getServerVersion(), s, getServletContext().getAliasName());
      setHeader(HeaderNames.entity_header_content_length, String
        .valueOf(body.length));
      // reset(boolean) method doesn't clear the flags if writer or output
      // stream is got that may cause getOutputStream() method to throw an
      // IllegalStateException if getWriter() method is called
      getOutStream().write(body);
      if (isGZip) {
        finish();
      } else {
        close();
      }
    }
  }

  public String encodeURL(String url, String logonGroup) {
    if (ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getZoneSeparator() == null) {
      return url;
    }
    String urlPrefix = null;
    if (url.startsWith("http://")) {
      int sepInd = url.indexOf('/', "http://".length());
      if (sepInd != -1) {
        urlPrefix = url.substring(0, sepInd);
        url = url.substring(sepInd);
      }
    } else if (url.startsWith("https://")) {
      int sepInd = url.indexOf('/', "https://".length());
      if (sepInd != -1) {
        urlPrefix = url.substring(0, sepInd);
        url = url.substring(sepInd);
      }
    }
    url = ParseUtils.canonicalize(url);
    if (("/" + url).startsWith(getServletRequest().getContextPath() + "/")) {
      url = "/" + url;
    }
    if (url.startsWith(getServletRequest().getContextPath() + "/")) {
      url = getServletRequest().getContextPath() + ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getZoneSeparator()
        + logonGroup + url.substring(getServletRequest().getContextPath().length());
    } else {
      try {
        url = makeAbsolute(url);
      } catch (MalformedURLException me) {
        if (traceLocation.beError()) {
          traceError("ASJ.web.000440", "sendRedirect", "cannot convert url", me, getServletContext().getApplicationName(), getServletContext().getCsnComponent());
        }
        throw new WebIllegalArgumentException(WebIllegalArgumentException.Cannot_be_converted_into_URL, new Object[]{url}, me);
      }
      if (url.startsWith("http://")) {
        int sepInd = url.indexOf('/', "http://".length());
        if (sepInd != -1) {
          url = url.substring(sepInd);
        }
      } else if (url.startsWith("https://")) {
        int sepInd = url.indexOf('/', "https://".length());
        if (sepInd != -1) {
          url = url.substring(sepInd);
        }
      }
      return encodeURL(url, logonGroup);
    }
    if (urlPrefix != null) {
      return urlPrefix + url;
    } else {
      return url;
    }
  }


  /**
   * Makes an URL as a valid request URL that will be used to redirect the request.
   *
   * @param s some URL
   * @return this URL as a valid request URL
   */
  private String makeAbsolute(String s) throws java.net.MalformedURLException {
    try {
      if (!isRelativeURL(s)) {
        return new URL(s).toString();
      }
    } catch (java.net.MalformedURLException ex) {
      traceDebug("makeAbsolute", "Invalid url is passed: [" + s + "].", ex);
    }
    String s1 = getRequestURL();
    URL url = new URL(new URL(s1), s);
    if (!ProtocolParser.isValidURL(s)) {
      throw new WebMalformedURLException(WebMalformedURLException.Not_valid_URL, new Object[]{s});
    }
    return url.toString();
  }

  private boolean isRelativeURL(String spec) {
    int i, limit, c;
    int start = 0;
    String newProtocol = null;
    boolean aRef = false;
    limit = spec.length();
    while ((limit > 0) && (spec.charAt(limit - 1) <= ' ')) {
      limit--;  //eliminate trailing whitespace
    }
    while ((start < limit) && (spec.charAt(start) <= ' ')) {
      start++;  // eliminate leading whitespace
    }

    if (spec.regionMatches(true, start, "url:", 0, 4)) {
      start += 4;
    }
    if (start < spec.length() && spec.charAt(start) == '#') {
      /* we're assuming this is a ref relative to the context URL.
      * This means protocols cannot start w/ '#', but we must parse
      * ref URL's like: "hello:there" w/ a ':' in them.
      */
      aRef = true;
    }
    for (i = start; !aRef && (i < limit) && ((c = spec.charAt(i)) != '/'); i++) {
      if (c == ':') {
        String s = spec.substring(start, i).toLowerCase();
        if (isValidProtocol(s)) {
          newProtocol = s;
          start = i + 1;
        }
        break;
      }
    }
    return newProtocol == null;
  }

  /*
  * Returns true if specified string is a valid protocol name.
  */
  private boolean isValidProtocol(String protocol) {
    int len = protocol.length();
    if (len < 1) {
      return false;
    }
    char c = protocol.charAt(0);
    if (!Character.isLetter(c)) {
      return false;
    }
    for (int i = 1; i < len; i++) {
      c = protocol.charAt(i);
      if (!Character.isLetterOrDigit(c) && c != '.' && c != '+' && c != '-') {
        return false;
      }
    }
    return true;
  }

  private String getRequestURL() {
    StringBuffer url = new StringBuffer();
    String scheme = getServletRequest().getScheme();
    int port = getServletRequest().getServerPort();
    url.append(scheme);
    url.append("://");
    url.append(getServletRequest().getServerName());
    if (scheme.equals("http") && port != 80 || scheme.equals("https") && port != 443) {
      url.append(':');
      url.append(port);
    }
    url.append(getServletRequest().getRequestURI());
    return (url.toString());
  }

  /**
   * Sets the length of the content body in the response In HTTP servlets, this method sets the HTTP Content-Length header.
   *
   * @param i length of the content
   */
  public void setContentLength(int i) {
    if (traceLocation.bePath()) {
      trace("setContentLength", "length = [" + i + "], isCommitted = [" + isCommitted() + "], isIncluded = [" + isIncluded + "]");
    }
    if (locked(false)) {
      return;
    }
    //setIntHeader() sets the contentLength also
    setIntHeader(HeaderNames.entity_header_content_length, i);
    output.useChunking = false;
  }

  public int getContentLength() {
    return contentLength;
  }

  /**
   * Compares the current and the new content type provided in setContentType
   * method. If the current content type is in its full form (includes character encoding)
   * while the new one is without character encoding
   * it is enough to check whether current content type starts with the new one provided.Thus the comparison
   * is made on the basis of the content type part. In all other cases in order to determine if the content type is
   * changed it is enough to check if the new content type starts with the current one.
   * @param currentContType the already set content type
   * @param newContType the new content type provided
   * @return true if the new content type is not  equal to the already set one
   */
  private boolean isContTypeChanged(String currentContType, String newContType) {
    // The following cases represent the probable combinations of ct(content type)
    // and ce(character encoding)provided in setContentType method:
    // currentContType -> ct1 ce1  |  ct1  |   ct1      |    ct1 ce1
    // newContTyp      -> ct1      |  ct2  |   ct2 ce2  |    ct2 ce2
    //                    (1)         (2)       (3)            (4)
    if(currentContType != null && newContType != null){
      boolean isCharEncodingIncluded_current =  currentContType.contains(";");
      boolean isCharEncodingIncluded_new = newContType.contains(";");
      if (!isCharEncodingIncluded_new && isCharEncodingIncluded_current){
        return !currentContType.startsWith(newContType);
      }
      return !newContType.startsWith(currentContType);
    }
    return true;
  }


  /**
   * Sets the content type of the response being sent to the client.
   *
   * @param s value of the new header content type
   */
  public void setContentType(String s) {
    if (traceLocation.bePath()) {
      trace("setContentType", "content type = [" + s + "], isCommitted = [" +
        isCommitted() + "], isIncluded = [" + isIncluded + "]");
    }

    // No effect if called after the response has been committed or included
    if (locked(false)) {
      String _contentType = getContentType();
      // If the new content type (s) is not equal to the already set one (_contentType),
      // write trace message with warning severity, otherwise - message with info severity
      // See CSN: 0001341580 2007 for more details
      if (isContTypeChanged(_contentType, s)) {
        if (traceLocation.beWarning()) {
          LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000549",
              "client [{0}] HttpServletResponseFacade.setContentType" +
        	  " [{1}] in application [{2}]: Suggested content type is ignored. Response is committed or included."+
        	  " Suggested content type = [{3}], available content type = [{4}], isCommitted = [{5}], isIncluded = [{6}]", new Object[]{getTraceClientId(), getObjectInstance(), getTraceAliasName(), s, _contentType, isCommitted(), isIncluded }, getServletContext().getApplicationName(), getServletContext().getCsnComponent());

        }
      } else {
        if (traceLocation.beInfo()) {
          traceInfo("setContentType",
              "Suggested content type is ignored. Response has been committed or included."
                  + " Suggested content type = [" + s
                  + "], available content type = [" + contentType
                  + "], isCommitted = [" + isCommitted() + "], isIncluded = ["
                  + isIncluded + "]");
        }
      }
      return;
    }

    if (s == null) {
      contentType = null;
      if (traceLocation.beWarning()) {
        LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000550",
    	    "client [{0}] HttpServletResponseFacade.setContentType" +
    	    " [{1}] in application [{2}]: method is called with incorrect content-type: {3}", new Object[]{ getTraceClientId(), getObjectInstance(), getTraceAliasName(), s}, getServletContext().getApplicationName(), getServletContext().getCsnComponent());

      }
      return;
    }

    contentType = s;

    //do not set the response's character encoding if it is called
    //after getWriter has been called or after the response has been committed.
    byte[] contentTypeBytes = s.getBytes();
    int charsetLocation = ByteArrayUtils.indexOf(contentTypeBytes, Constants.charset);
    int semi = ByteArrayUtils.indexOf(contentTypeBytes, (byte) ';', charsetLocation);
    boolean hasCharset = false;
    if (!getWriter) {

      if (charsetLocation > -1) {
        String charsetValue = null;
        if (semi > -1) {
          charsetValue = contentType.substring(charsetLocation + 8, semi).trim();
        } else {
          charsetValue = contentType.substring(charsetLocation + 8).trim();
        }
        //charset value may be quoted, but must not contain any quotes.
        if (charsetValue != null && charsetValue.length() > 0) {
          charsetValue = charsetValue.replace('"', ' ').trim();
          try {
            byte buffer[] = {(byte) 'a'};
            new String(buffer, charsetValue);
            characterEncoding = charsetValue;
            hasCharset = true;
            isCharacterEncodingSet = true;
          } catch (UnsupportedEncodingException e) {
            if (traceLocation.beWarning()) {
              String msg = LogContext.getExceptionStackTrace(new Exception("method is called with unsupported encoding: " + charsetValue));
              LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000551",
                  "client [{0}] HttpServletResponseFacade.setContentType" +
            	  " [{1}] in application [{2}]: {3}", new Object[]{getTraceClientId(), getObjectInstance(), getTraceAliasName(), msg},  getServletContext().getApplicationName(), getServletContext().getCsnComponent());


            }
          }
        }
      }

      if (!hasCharset && characterEncoding != null) {
        contentType += ";" + new String(Constants.charset) + characterEncoding;
      }

      if (traceLocation.bePath()) {
        if (hasCharset || (!hasCharset && (characterEncoding != null))) {
          trace("setContentType", "content type = [" + contentType
              + "], charset = [" + characterEncoding + "], isCommitted = ["
              + isCommitted() + "], getWriter = [" + getWriter + "]");
        }
      }
    } else {
      if (characterEncoding != null && isCharacterEncodingSet) {
        if (charsetLocation > -1) {
          //if charset is specified then we must get substring before ';charset'
          //otherwise if we get substring before 'charset', one ';' will remain
          contentType = contentType.substring(0, charsetLocation - 1);
        }
        contentType += ";" + new String(Constants.charset) + characterEncoding;
      }
      if (charsetLocation > -1){
        if (characterEncoding == null || !s.endsWith(characterEncoding)) {
          if (traceLocation.beWarning()) {
             LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000552",
        	     "client [{0}] HttpServletResponseFacade.setContentType" +
        	     " [{1}] in application [{2}]: Method has no effect on the character encoding!"+
        	     " content type = [{3}] charset = [{4}], getWriter = [{5}], isCommitted = [{6}], isIncluded = [{7}]", new Object[]{getTraceClientId(), getObjectInstance(), getTraceAliasName() , s, characterEncoding, getWriter, isCommitted(), isIncluded}, getServletContext().getApplicationName(), getServletContext().getCsnComponent());

          }
        } else {
          if (traceLocation.beInfo()) {
            traceInfo("setContentType",
                "Method has no effect on the character encoding!"
                    + " content type = [" + s + "] charset = ["
                    + characterEncoding + "], getWriter = [" + getWriter
                    + "], isCommitted = [" + isCommitted()
                    + "], isIncluded = [" + isIncluded + "]");
          }
        }
      }
    }

    char[] ct = contentType.toCharArray();
    byte[] ctBytes = new byte[ct.length];

    for (int i = 0; i < ct.length; i++) {
      ctBytes[i] = (byte) ct[i];
    }

    setHeader(HeaderNames.entity_header_content_type_, ctBytes);
  }

  /**
   * Sets the character encoding (MIME charset) of the response being sent to the
   * client, for example, to UTF-8. If the character encoding has already been set
   * by setContentType(String) or setLocale(Locale) , this method overrides
   * it.
   *
   * @param s a String specifying only the character set defined by IANA
   *          Character Sets.
   * @see javax.servlet.ServletResponse#setCharacterEncoding(String)
   */
  public void setCharacterEncoding(String s) {
    if (traceLocation.bePath()) {
      trace("setCharacterEncoding", "charset = [" + s + "], isCommitted = [" +
        isCommitted() + "], getWriter = [" + getWriter + "]");
    }

    //This method can be called repeatedly to change the character encoding. This
    //method has no effect if it is called after getWriter has been called or after the
    //response has been committed.
    if (isCommitted() || getWriter) {
      if (traceLocation.beWarning()) {
    	LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000553",
    	    "client [{0}] HttpServletResponseFacade.setCharacterEncoding" +
    	    " [{1}] in application [{2}]: Method has no effect on the Character Encoding!" +
    	    " charset = [{3}], isCommitted = [{4}], getWriter = [{5}]", new Object[]{getTraceClientId(), getObjectInstance(), getTraceAliasName(), s, isCommitted(), getWriter}, getServletContext().getApplicationName(), getServletContext().getCsnComponent());


      }
      return;
    }

    if (s == null || s.equals("")) {
      if (traceLocation.beWarning()) {
    	String msg = LogContext.getExceptionStackTrace(new Exception("method is called with empty encoding: " + s));
    	LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000554",
    	    "client [{0}] HttpServletResponseFacade." + "setCharacterEncoding" +
    	    " [{1}] in application [{2}]: {3}", new Object[]{getTraceClientId(), getObjectInstance(), getTraceAliasName(), msg}, getServletContext().getApplicationName(), getServletContext().getCsnComponent());

      }
      return;
    }
    //  Ensure that the specified encoding is valid
    try {
      byte buffer[] = {(byte) 'a'};
      new String(buffer, s);
    } catch (UnsupportedEncodingException e) {
      if (traceLocation.beWarning()) {
    	String msg = LogContext.getExceptionStackTrace(new Exception("method is called with unsupported encoding: " + s));
    	LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000555",
    	    "client [{0}] HttpServletResponseFacade.setCharacterEncoding" +
    	    " [{1}] in application [{2}]: {3}", new Object[]{getTraceClientId(), getObjectInstance(), getTraceAliasName(), msg}, getServletContext().getApplicationName(), getServletContext().getCsnComponent());


      }
      return;
    }

    //Save charset for later use
    updateCharacterEncoding(s);
  }

  private void updateCharacterEncoding(String s) {
    this.characterEncoding = s.replace('"', ' ').trim();
    isCharacterEncodingSet = true;

    byte[] contentTypeBytes = getHeader(HeaderNames.entity_header_content_type_);

    if (contentTypeBytes != null) {
      char[] contentTypeChars = new char[contentTypeBytes.length];

      for (int i = 0; i < contentTypeBytes.length; i++) {
        contentTypeChars[i] = (char) (contentTypeBytes[i] & 0x00ff);
      }

      String tempContentType = new String(contentTypeChars);
      int charsetLocation = ByteArrayUtils.indexOf(contentTypeBytes, Constants.charset);
      int semi = ByteArrayUtils.indexOf(contentTypeBytes, (byte) ';', charsetLocation);

      if (charsetLocation > -1) {
        if (semi > -1) {
          String tail = tempContentType.substring(semi);
          tempContentType = tempContentType.substring(0, charsetLocation + 8) + characterEncoding;
          tempContentType += tail;
        } else {
          tempContentType = tempContentType.substring(0, charsetLocation + 8) + characterEncoding;
        }
      } else {
        tempContentType += ";" + new String(Constants.charset) + characterEncoding;
      }

      //save content type for later use;
      contentType = tempContentType;
      if (traceLocation.bePath()) {
        trace("updateCharacterEncoding", "update content type = [" + contentType +
          "], isCommitted = [" + isCommitted() + "], getWriter = [" + getWriter + "]");
      }

      char[] ct = contentType.toCharArray();
      byte[] ctBytes = new byte[ct.length];

      for (int i = 0; i < ct.length; i++) {
        ctBytes[i] = (byte) ct[i];
      }

      setHeader(HeaderNames.entity_header_content_type_, ctBytes);
    }
  }

  /**
   * Sets the preferred buffer size for the body of the response. The servlet container
   * will use a buffer at least as large as the size requested. The actual buffer size
   * used can be found using getBufferSize.
   *
   * @param size the preferred buffer size
   */
  public void setBufferSize(int size) {
    if (traceLocation.bePath()) {
      trace("setBufferSize", "size = [" + size + "]");
    }
    if (size < 0) {
      throw new WebIllegalStateException(WebIllegalStateException.ILLEGAL_BUFFER_SIZE_VALUE, new Object[]{"" + size});
    }
    boolean isWritenAnything = false;
    if (isGZip) {
      isWritenAnything = gzipprintwriter.isWrittenAnything() || gzipResponseStream.isWrittenAnything();
    } else {
      isWritenAnything = output.isWritenAnything() || printwriter.isWrittenAnything();
    }
    if (isWritenAnything) {
      if (traceLocation.beWarning()) {
        LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000556",
                "client [{0}] HttpServletResponseFacade.setBufferSize" +
                " [{1}] in application [{2}]: isWritenAnything = [{3}], throwing exception", new Object[]{getTraceClientId(),  getObjectInstance(), getTraceAliasName(), isWritenAnything}, getServletContext().getApplicationName(), getServletContext().getCsnComponent());

      }
      throw new WebIllegalStateException(WebIllegalStateException.Cannot_use_setBufferSize_if_anything_has_written_in_the_ServletOutputStream);
    }
    if (isGZip) {
      gzipResponseStream.setBufferSize(size);
    } else {
      output.setBufferSize(size);
    }
    if (getWriter) {
      if (isGZip) {
        gzipprintwriter.setBufferSize(size);
      } else {
        printwriter.setBufferSize(size);
      }
    }
  }

  /**
   * Returns the actual buffer size used for the response. If no buffering is used, this method returns 0.
   *
   * @return the actual buffer size used
   */
  public int getBufferSize() {
    if (traceLocation.beDebug()) {
      traceDebug("getBufferSize", "size = [" + output.getBufferSize() + "]");
    }
    return output.getBufferSize();
  }

  /**
   * Clears the buffer form all previously written data.
   */
  public void clearBuffer() throws IOException {
    if (!isGZip) {
      output.clearBuffer();
    }
    if (getWriter) {
      if (isGZip) {
        gzipprintwriter.reset();
      } else {
        printwriter.reset();
      }
    }
  }

  /**
   * Forces any content in the buffer to be written to the client. A call to this method
   * automatically commits the response, meaning the status code and headers will be written.
   */
  public void flushBuffer() throws IOException {
    if (traceLocation.bePath()) {
      trace("flushBuffer", "getWriter = [" + getWriter + "], isGZip = [" + isGZip + "]");
    }
    if (getWriter) {
      if (isGZip) {
        gzipResponseStream.flush(true);
      } else {
        output.flush(true);
      }
    }
    if (isGZip) {
      gzipResponseStream.flush();
    } else {
      output.flush();
    }
  }

  /**
   * Flushes the Output Stream.
   *
   * @throws IOException
   */
  public void finish() throws IOException {
    if (traceLocation.bePath()) {
      tracePath("finish", "");
    }
    isClosed = true;
    output.setFlushMode(false);
    if (isGZip) {
      gzipResponseStream.setFlushMode(false);
    }
    if (isGZip && getWriter) {
      gzipprintwriter.close();
    } else if (getWriter) {
      printwriter.close();
    } else if (isGZip) {
      gzipResponseStream.close();
    }
    output.close();
    setConnectionType(output.isChunked());
  }

  public void resetBuffer() throws IllegalStateException {
    if (traceLocation.bePath()) {
      trace("resetBuffer", "isCommitted = [" + isCommitted() + "]");
    }
    if (isCommitted()) {
      throw new WebIllegalStateException(WebIllegalStateException.Stream_is_already_commited);
    }
    try {
      clearBuffer();
    } catch (IOException io) {
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000197",
        "Error in clearing the buffers of the response.", io, getServletContext().getApplicationName(), getServletContext().getCsnComponent());
    }
  }


  public boolean isClosed() {
    return isClosed;
  }

  public void setServletName(String servlet) {
    servletName = servlet;
  }

  public String getServletName() {
    return servletName;
  }

  private boolean doError(int code, String message, String comingFrom) throws ServletException, IOException {
    boolean errorHandler = false;
    if (getServletRequest().isErrorHandler()) {
      if (getServletRequest().getServletName().equals(Constants.ERROR_HANDLER_SERVLET)) {
        //To prevent cycling if the error page throws an error too
        //The error handler is set to true only in
        //- HttpHandlerImpl.processErrorPage(...)
        //- HttpHandlerImpl.processError(...)
        //- This method 
        //Because of this we will return empty response
        if (LogContext.getLocationServletResponse().beWarning()) {
          LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000704",
            "The default global error handler [SapErrorHandlerServlet] throws an exception and cannot return an error response. " + 
            "An empty response will be returned.", null, null);
        }
        return false;
      } else {
        errorHandler = true;
      }
    }

    //Try to find error handler provided by application itself.
    String errPageLocation = null;
    if (comingFrom.equals("sendError") && !errorHandler) {
      errPageLocation = getServletContext().getErrorPage(code);
    }

    ServletContext targetContext = null;
    boolean global = false;
    if (errPageLocation == null) {
      errPageLocation = getServiceContext().getWebContainerProperties().getErrorPageLocation();
      
      if (errorHandler && getServletRequest().getServletName().equals(errPageLocation)) {
        if (LogContext.getLocationServletResponse().beWarning()) {
          LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000710",
            "The custom global error handler cannot return an error response, because of this the default global error handler will do it. It's configuration is the following: "
            + "[Global_app_config/error_page/location = {0}], [Global_app_config/error_page/context_root = {1}].",
            new Object[]{getServiceContext().getWebContainerProperties().getErrorPageLocation(), 
            getServiceContext().getWebContainerProperties().getErrorPageContextRoot()}, null, null);
        }
        errPageLocation = Constants.ERROR_HANDLER_SERVLET;
      } else {
        //There is no error handler provided by application
        if (!errPageLocation.equals(Constants.ERROR_HANDLER_SERVLET)) {
          //If the global error handler is not provided by us then get the specified web module
          String targetContextName = getServiceContext().getWebContainerProperties().getErrorPageContextRoot();
          if (targetContextName != null && !targetContextName.equals("") && !targetContextName.startsWith("/")) {
            targetContextName = "/" + targetContextName;
          }
          targetContext = getServletContext().getServletContext().getContext(targetContextName);
          if (targetContext == null) {
            if (LogContext.getLocationServletResponse().beWarning()) {
              LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000703",
                "Web Container cannot resolve the custom global error handler, because of this the default global error handler is used. The current configuration is: "
                + "[Global_app_config/error_page/location = {0}], [Global_app_config/error_page/context_root = {1}]. "
                + "Possible reason: The web application that is specified in the property is not deployed.",
                new Object[]{getServiceContext().getWebContainerProperties().getErrorPageLocation(), 
                getServiceContext().getWebContainerProperties().getErrorPageContextRoot()}, null, null);
            }
            errPageLocation = Constants.ERROR_HANDLER_SERVLET;
          }
        }
      }
      
      if (errPageLocation.equals(Constants.ERROR_HANDLER_SERVLET)) {
        if (!((ApplicationContext) getServletContext()).getWebMappings().isServletMappingExist(Constants.ERROR_HANDLER_SERVLET.substring(1), Constants.ERROR_HANDLER_SERVLET)){
          //This must not happen!
          return false;
        }
      }
      
      global = true;
    }

    if (errPageLocation != null) {
      if (traceLocation.beDebug()) {
        traceDebug("doError", "code = [" + code + "], message = [" + message +
          "], errPageLocation = [" + errPageLocation + "], servlet_name = [" + servletName + "]");
      }

      getServletRequest().setErrorHandler(true);
      getServletRequest().setAttribute("javax.servlet.error.status_code", new Integer(code));
      getServletRequest().setAttribute("javax.servlet.error.message", message);
      getServletRequest().setAttribute("javax.servlet.error.request_uri", getServletRequest().getRequestURI());
      getServletRequest().setAttribute("javax.servlet.error.servlet_name", servletName);
      if (global) {
        Throwable ex = getServletRequest().getHttpParameters().getErrorData().getException();
        if (ex != null) {
          getServletRequest().setAttribute("javax.servlet.error.exception", ex);
          getServletRequest().setAttribute("javax.servlet.error.exception_type", ex.getClass());
        }
      }

      String url = getURIForGeneratingErrorReport(getServletRequest().getHttpParameters().getErrorData());
      if (url != null) {
        getServletRequest().setAttribute(Constants.URI_FOR_GENERATING_ERROR_REPORT, url);
      }

      RequestDispatcher requestDispatcher;
      if (targetContext == null) {
        requestDispatcher = getServletContext().getServletContext().getRequestDispatcher(errPageLocation);
      } else {
        requestDispatcher = targetContext.getRequestDispatcher(errPageLocation);
      }
          
      if (getServletRequest().getHttpParameters().getErrorData().isHtmlAllowed() && LogContext.getLocationServletResponse().beInfo()) {
        LogContext.getLocationServletResponse().infoT("Web Container will return an error response where HTML is allowed.");
      }

      if (isCommitted()) {
        requestDispatcher.include(getServletRequest(), this);
      } else {
        requestDispatcher.forward(getServletRequest(), this);
      }
      
      return true;
    }
    
    return false;
  }//end of doError(int code, String message, String comingFrom)

  public String checkReset() {
    String result = super.checkReset();
    if (contentLength != -1) {
    	result += "contentLength=[" + contentLength + "] is not reset to -1;";
    }
    return result;
  }

  /**
   * @return Returns the modifiable.
   */
  public boolean isModifiable() {
    return modifiable;
  }

  /**
   * @param modifiable The modifiable to set.
   */
  public void setModifiable(boolean modifiable) {
    if (traceLocation.bePath()) {
      tracePath("setModifiable", "modifiable = [" + modifiable + "]");
    }
    this.modifiable = modifiable;
  }

  /**
   * Returns an absolute URI with the given scheme and path and adequate
   * host and port.
   * <p/>
   * The method accepts only absolute paths, e.g. such that start with a
   * leading '/' and interprets them as relative to the servlet container root.
   *
   * @param scheme The required scheme. Allowed values are "http" and "https"
   * @param path   An absolute path that start with a leading '/'
   * @return An absolute URI or <code>null</code> in case that servlet container
   *         can not find adequate host and port
   */
  public String getURLForScheme(String scheme, String path) {
    // Scheme limitations check
    if (scheme == null) {
      throw new WebIllegalArgumentException("Invalid scheme \"null\". Scheme can not be null.");
    }
    scheme = scheme.toLowerCase();
    if (scheme != "http" && scheme != "https") {
      throw new WebIllegalArgumentException("Invalid scheme \"" + scheme
        + "\". Allowed values are \"http\" and \"https\"");
    }

    // Path limitations check
    if (path == null) {
      throw new WebIllegalArgumentException("Invalid path \"" + path
        + "\". Path can not be null");
    } else if (!path.startsWith("/")) {
      throw new WebIllegalArgumentException("Invalid path \"" + path
        + "\". Path must start with \"/\"");
    }

    String serverURL = getHttpParameters().getServerURL(scheme);
    if (serverURL == null) {
      return null;
    }
    return serverURL + path;
  }

  /**
   * Returns an absolute URI that repeats the current request URI but with
   * the given scheme and adequate host and port
   *
   * @param scheme The required scheme. Allowed values are "http" and "https"
   * @return An absolute URI or <code>null</code> in case that servlet container
   *         can not find adequate host and port
   */
  public String getRequestURLForScheme(String scheme) {
    String queryString = getServletRequest().getQueryString();
    if (queryString == null) {
      return getURLForScheme(scheme, getServletRequest().getRequestURIinternal());
    } else {
      StringBuffer path =
        new StringBuffer(getServletRequest().getRequestURIinternal());
      path.append('?');
      path.append(queryString);
      return getURLForScheme(scheme, path.toString());
    }
  }

  protected void commit(boolean useChunking) throws IOException {
    if (status == 304 || status == 204 || status < 200) {
      output.clearBuffer();
      // these responses don't contain body, so we should consider chunk off
      if (useChunking) {
        output.useChunking = false;
        useChunking = false;
      }
    }
    super.commit(useChunking);
  }

  public void setResponseLengthLog(int totalCount) {
    getServletRequest().setResponseLengthLog(totalCount);
  }

  /**
   * This method returns ID of the current server node to be used by applications.
   * Note that node does not match the actual persistent server node ID and the format
   * may vary.
   * @return integer alias representing ID of the current node
   */
  public int getServerNodeAliasID() {
    return ServiceContext.getServiceContext().getServerId();
  }

  /**
   * The method returns an URI for generating error report that can be embedded into the custom error response.
   * If the Web Container's property 'GenerateErrorReport' is not enabled this method will return <code>NULL</code>.
   *
   * @param errorData the error data.
   * @return URI that can be embedded into the error response
   * in the following format '/@@@GenerateErrorReport@@@?id=<the-id-of-the-error-report>'.
   * If the Web Container's property 'GenerateErrorReport' is not enabled this method will return <code>NULL</code>.
   */
  public String getURIForGeneratingErrorReport(ErrorData errorData) {
    if (errorData == null) {
      if (traceLocation.beDebug()) {
        traceDebug("getURIForGeneratingErrorReport", "errorData=null");
      }
      return null;
    }
    
    String logIdISE500 = "";
    if (ResponseCodes.code_internal_server_error == errorData.getErrorCode()) { 
      logIdISE500 = traceError500(errorData);
    }

    boolean generateErrorReports = ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().isGenerateErrorReports();
    String id = "";
    if (generateErrorReports && errorData.getSupportabilityData().isUserActionNeeded()) {
      String details = errorData.getAdditionalMessage();

      if (errorData.getSupportabilityData().getLogId().equals("") && details != null) {
        int ind = details.indexOf("log ID");
        if (ind != -1) {
          errorData.getSupportabilityData().setLogId(details.substring(details.indexOf("[", ind), details.indexOf("]", ind) + 1));
        }
      }

      long time = System.currentTimeMillis();
      id = (time + getServletRequest().getHttpParameters().getRequest().getClientId()) + "";
      Session session = (Session) getServletRequest().getHttpParameters().getApplicationSession();
      String jsessionId = (session != null) ? session.sessionId() : "";
      ErrorReportInfoBean errorReportInfoBean = new ErrorReportInfoBean(getServletRequest().getRemoteAddr(), jsessionId, time, getServletContext().getAliasName(), 
        errorData.getErrorCode(), errorData.getSupportabilityData());
      if (logIdISE500 != null && !logIdISE500.equals("")) {
        errorReportInfoBean.setLogIdISE500(logIdISE500);
      }
      ServiceContext.getServiceContext().getHttpProvider().getErrorReportInfos().put(id, errorReportInfoBean);

      return "/@@@GenerateErrorReport@@@?id=" + id;
    } else {
      return null;
    }
  }//end of getURIForGeneratingErrorReport(ErrorData errorData)

  /**
   * The method traces the Internal Server Error 500 in the default traces with
   * - Severity: Error
   * - Location: com.sap.engine.services.servlets_jsp.ISE500
   * - Message ID: com.sap.ASJ.web.000500
   * - Message: '500 Internal Server Error will be returned for HTTP request [{0}]: component [{1}], web module [{2}], application [{3}], problem categorization [{4}], internal categorization [{5}].'
   *
   * @param errorData the error data that will be used to trace the Internal Server Error 500.
   * @return the ID of the trace record or <code>NULL</code> if a trace record is not created.
   */
  public String traceError500(ErrorData errorData) {
    if (errorData == null) {
      if (traceLocation.beDebug()) {
        traceDebug("traceError500", "errorData=null");
      }
      return null;
    }
    
    String aliasName = getServletContext().getAliasName();
    String applicationName = getServletContext().getApplicationName();
    int categorizationId = ErrorReportInfoBean.getInternalCategorization(ResponseCodes.code_internal_server_error, errorData.getSupportabilityData(), aliasName, applicationName);
    if (ServiceContext.getServiceContext().getWebMonitoring().isMonitoringStarted()) {
        ServiceContext.getServiceContext().getWebMonitoring().addCategorizationID(categorizationId);
      }
    return LogContext.getLocation(LogContext.LOCATION_ISE_500).traceError("ASJ.web.000500",
      "500 Internal Server Error is returned for HTTP request [{0}]:\r\n  component [{1}],\r\n  " + //TODO : Vily G : log components different from servlet or not?
      "web module [{2}],\r\n  application [{3}],\r\n  DC name [{4}],\r\n  CSN component[{5}],\r\n  " +
      "problem categorization [{6}],\r\n  internal categorization [{7}].\r\n",
      new Object[] {getServletRequest().getRequestURL(), getServletName(), aliasName,
      applicationName, errorData.getSupportabilityData().getDcName(), errorData.getSupportabilityData().getCsnComponent(), errorData.getSupportabilityData().getMessageId(),
      categorizationId},
      errorData.getException(), errorData.getSupportabilityData().getDcName(), errorData.getSupportabilityData().getCsnComponent());
   }//end of traceError500(ErrorData errorData)

}
