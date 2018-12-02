package com.sap.engine.services.webservices.espbase.client.transport.local;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUtils;

import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.tools.ReferenceByteArrayOutputStream;

/**
 * @version 1.0
 * @author Ivaylo Zlatanov, ivaylo.zlatanov@sap.com
 * 
 */

public class LocalHttpServletRequest implements HttpServletRequest {

  private Hashtable<String, String[]> headers;

  private Hashtable<String, String[]> parameters;

  private ReferenceByteArrayOutputStream body;

  private String requestMethod;

  private URL endpointURL;
  
  private ClientConfigurationContext clientContext;

  public LocalHttpServletRequest(String url, String reqMethod) {
    try {
      if (url.indexOf("http:") < 0) {
        url = "http://localhost:80" + url; // dummy port; necessary for URL object;
      }
      endpointURL = new URL(url);
    } catch (MalformedURLException murle) {
      throw new RuntimeException(murle);
    }
    headers = new Hashtable<String, String[]>();
    parameters = new Hashtable<String, String[]>();
    body = new ReferenceByteArrayOutputStream();
    requestMethod = reqMethod;
    if (endpointURL.getQuery() != null) {
      parameters = HttpUtils.parseQueryString(endpointURL.getQuery());
    }
  }

  public String getAuthType() {
    throw new UnsupportedOperationException("getAuthType()");
  }

  public String getContextPath() {
    throw new UnsupportedOperationException("getContextPath()");
  }

  public Cookie[] getCookies() {
    throw new UnsupportedOperationException("getCookies()");
  }

  public long getDateHeader(String arg0) {
    throw new UnsupportedOperationException("getDateHeader()");
  }

  public String getHeader(String arg0) {
    String[] values = (String[]) headers.get(arg0);
    if (values == null || values.length < 1) {
      return null;
    }
    return values[0];
  }

  public Enumeration<String[]> getHeaderNames() {
    return headers.elements();
  }

  public Enumeration<String[]> getHeaders(String arg0) {
    String[] values = (String[]) headers.get(arg0);

    if (values == null) {
      List<String[]> emptyList = Collections.emptyList();
      return Collections.enumeration(emptyList);
    } else {
      List l = Arrays.asList(values);
      return Collections.enumeration(l);
    }
  }

  public int getIntHeader(String arg0) {
    throw new UnsupportedOperationException("getIntHeader()");
  }

  public String getMethod() {
    return requestMethod;
  }

  public String getPathInfo() {
    throw new UnsupportedOperationException("getPathInfo()");
  }

  public String getPathTranslated() {
    throw new UnsupportedOperationException("getPathTranslated()");
  }

  public String getQueryString() {
    throw new UnsupportedOperationException("getQueryString()");
  }

  public String getRemoteUser() {
    throw new UnsupportedOperationException("getRemoteUser()");
  }

  public String getRequestURI() {
    return endpointURL.getPath();
  }

  public StringBuffer getRequestURL() {
    return new StringBuffer(endpointURL.getPath());
  }

  public String getRequestedSessionId() {
    throw new UnsupportedOperationException("getRequestedSessionId()");
  }

  public String getServletPath() {
    return endpointURL.getPath();
  }

  public HttpSession getSession(boolean arg0) { // called in DefaultImplementationContainer
    return null;
  }

  public HttpSession getSession() {
    return null;
  }

  public Principal getUserPrincipal() { // called in ServletDispatcherImpl
    return null;
  }

  public boolean isRequestedSessionIdFromCookie() {
    throw new UnsupportedOperationException("isRequestedSessionIdFromCookie()");
  }

  public boolean isRequestedSessionIdFromURL() {
    throw new UnsupportedOperationException("isRequestedSessionIdFromURL()");
  }

  public boolean isRequestedSessionIdFromUrl() {
    throw new UnsupportedOperationException("isRequestedSessionIdFromUrl()");
  }

  public boolean isRequestedSessionIdValid() {
    throw new UnsupportedOperationException("isRequestedSessionIdValid()");
  }

  public boolean isUserInRole(String arg0) {
    throw new UnsupportedOperationException("isUserInRole(String)");
  }

  public Object getAttribute(String arg0) {
    throw new UnsupportedOperationException("getAttribute(String)");
  }

  public Enumeration<String[]> getAttributeNames() {
    throw new UnsupportedOperationException("getAttributeNames()");
  }

  public String getCharacterEncoding() { // ?
    return "UTF-8";
  }

  public int getContentLength() {
    return body.size();
  }

  public String getContentType() {
    String[] value = (String[]) headers.get("Content-Type");
    return (value == null || value.length < 1) ? null : value[0];
  }

  public ServletInputStream getInputStream() throws IOException {
    return new LocalServletInputStream(new ByteArrayInputStream(body
        .getContentReference(), 0, body.size()));
  }

  /*public ByteArrayInputStream getBodyInputStream() { // check if called
    return new ByteArrayInputStream(body.getContentReference(), 0, body.size());
  }*/

  public ByteArrayOutputStream getBodyOutputStream() {
    return body;
  }

  public String getLocalAddr() {
    throw new UnsupportedOperationException("getLocalAddr()");
  }

  public String getLocalName() {
    throw new UnsupportedOperationException("getLocalName()");
  }

  public int getLocalPort() {
    throw new UnsupportedOperationException("getLocalPort");
  }

  public Locale getLocale() {
    throw new UnsupportedOperationException("getLocale()");
  }

  public Enumeration getLocales() {
    throw new UnsupportedOperationException("getLocales()");
  }

  public String getParameter(String arg0) {
    String[] vals = (String[]) parameters.get(arg0);
    if (vals == null || vals.length < 1) {
      return null;
    }
    return vals[0];
  }

  public Map<String, String[]> getParameterMap() {
    return Collections.unmodifiableMap(parameters);
  }

  public Enumeration<String> getParameterNames() {
    return parameters.keys();
  }

  public String[] getParameterValues(String arg0) {
    return (String[]) parameters.get(arg0);
  }

  public String getProtocol() {
    return "HTTP/1.1";
  }

  public BufferedReader getReader() throws IOException {
    throw new UnsupportedOperationException("getReader()");
  }

  public String getRealPath(String arg0) {
    throw new UnsupportedOperationException("getRealPath()");
  }

  public String getRemoteAddr() {
    throw new UnsupportedOperationException("getRemoteAddr()");
  }

  public String getRemoteHost() {
    throw new UnsupportedOperationException("getRemoteHost()");
  }

  public int getRemotePort() {
    throw new UnsupportedOperationException("getRemotePort()");
  }

  public RequestDispatcher getRequestDispatcher(String arg0) {
    throw new UnsupportedOperationException("getRequestDispatcher()");
  }

  public String getScheme() {
    throw new UnsupportedOperationException("getScheme()");
  }

  public String getServerName() {
    throw new UnsupportedOperationException("getServerName()");
  }

  public int getServerPort() {
    throw new UnsupportedOperationException("getServerPort()");
  }

  public boolean isSecure() {
    return false;
  }

  public void removeAttribute(String arg0) {
    throw new UnsupportedOperationException("removeAttribute(String)");
  }

  public void setAttribute(String arg0, Object arg1) {
    throw new UnsupportedOperationException("setAttribute(String, Object)");
  }

  public void setCharacterEncoding(String arg0)
      throws UnsupportedEncodingException {
    throw new UnsupportedOperationException("setCharacterEncoding(String)");
  }

  public void setHeader(String headerName, String[] headerValues) {
    headers.put(headerName, headerValues);
  }

  public Hashtable<String, String[]> getHeaders() {
    return headers;
  }

  public void setClientContext(ClientConfigurationContext context){
    clientContext = context;
  }
  
  public ClientConfigurationContext getClientConfigurationContext(){
    return clientContext;
  }
}