package com.sap.engine.services.webservices.espbase.client.transport.local;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import com.sap.engine.services.webservices.tools.ReferenceByteArrayOutputStream;

/**
 * @version 1.0
 * @author Ivaylo Zlatanov, ivaylo.zlatanov@sap.com
 * 
 */

public class LocalHttpServletResponse implements HttpServletResponse {
  private Hashtable<String, String[]> headers;

  private int statusCode;

  private String statusMessage;

  private ReferenceByteArrayOutputStream body;

  public LocalHttpServletResponse() {
    body = new ReferenceByteArrayOutputStream();
    headers = new Hashtable<String, String[]>();
  }

  public void addCookie(Cookie arg0) {
    throw new UnsupportedOperationException("addCookie(Cookie)");
  }

  public void addDateHeader(String arg0, long arg1) {
    headers.put(arg0, new String[] { new Date(arg1).toString() });
  }

  public void addHeader(String arg0, String arg1) {
    headers.put(arg0, new String[] { arg1 });
  }

  public void addIntHeader(String arg0, int arg1) {
    throw new UnsupportedOperationException("addIntHeader(String, int");
  }

  public boolean containsHeader(String arg0) {
    return headers.get(arg0) != null;
  }

  public String encodeRedirectURL(String arg0) {
    throw new UnsupportedOperationException("encodeRedirectURL(String)");
  }

  public String encodeRedirectUrl(String arg0) {
    throw new UnsupportedOperationException("encodeRedirectUrl(String)");
  }

  public String encodeURL(String arg0) {
    throw new UnsupportedOperationException("encodeURL(String)");
  }

  public String encodeUrl(String arg0) {
    throw new UnsupportedOperationException("encodeUrl()");
  }

  public void sendError(int arg0) throws IOException {
    if (isCommitted()) {
      throw new IllegalStateException("Response already commited");
    }
    reset();
    setStatus(arg0);
  }

  public void sendError(int arg0, String arg1) throws IOException {
    sendError(arg0);
    setStatus(arg0, arg1);
  }

  public void sendRedirect(String arg0) throws IOException {
    throw new UnsupportedOperationException("sendRedirec(String)");
  }

  public void setDateHeader(String arg0, long arg1) {
    headers.put(arg0, new String[] { new Date(arg1).toString() });
  }

  public void setHeader(String arg0, String arg1) {
    headers.put(arg0, new String[] { arg1 });
  }

  public void setIntHeader(String arg0, int arg1) {
    headers.put(arg0, new String[] { "" + arg1 });
  }

  public int getStatus() {
    return statusCode;
  }

  public void setStatus(int arg0) {
    statusCode = arg0;
  }

  public void setStatus(int arg0, String arg1) {
    statusCode = arg0;
    statusMessage = arg1;
  }

  public String getResponseMessage() {
    return statusMessage;
  }

  public void flushBuffer() throws IOException { // ??

  }

  public int getBufferSize() {
    throw new UnsupportedOperationException("getBufferSize()");
  }

  public String getCharacterEncoding() {
    return "UTF-8";
  }

  public String getContentType() {
    String[] values = (String[]) headers.get("Content-Type");
    if (values == null || values.length < 1) {
      return null;
    }
    return values[0];
  }

  public Locale getLocale() {
    throw new UnsupportedOperationException("getLocale()");
  }

  public ServletOutputStream getOutputStream() throws IOException {
    return new LocalServletOutputStream(body);
  }

  public InputStream getBodyInputStream() {
    return new ByteArrayInputStream(body.getContentReference(), 0, body.size()
        /*.getContentReference().length*/);
  }

  public PrintWriter getWriter() throws IOException {
    throw new UnsupportedOperationException("getWriter()");
  }

  public boolean isCommitted() {
    return false;
  }

  public void reset() {
    headers.clear();
    statusCode = 0;
    statusMessage = "";
    body.reset();
  }

  public void resetBuffer() {
    body.reset();
  }

  public void setBufferSize(int arg0) {
    throw new UnsupportedOperationException("setBufferSize(int)");
  }

  public void setCharacterEncoding(String arg0) {
    throw new UnsupportedOperationException("setCharacterEncoding(String)");
  }

  public void setContentLength(int arg0) {
    headers.put("Content-Length", new String[] { "" + arg0 });
  }

  public void setContentType(String arg0) {
    headers.put("Content-Type", new String[] { arg0 });
  }

  public void setLocale(Locale arg0) {
    throw new UnsupportedOperationException("setLocale(Locale)");
  }

  public Enumeration<String[]> getHeaders(String arg0) {
    String[] values = (String[]) headers.get(arg0);
    List<String[]> emptyList = Collections.emptyList();
    List l = Arrays.asList(values);
    return Collections.enumeration(values == null ? emptyList : l);
  }

  public Hashtable<String, String[]> getHeaders() {
    return headers;
  }
}
