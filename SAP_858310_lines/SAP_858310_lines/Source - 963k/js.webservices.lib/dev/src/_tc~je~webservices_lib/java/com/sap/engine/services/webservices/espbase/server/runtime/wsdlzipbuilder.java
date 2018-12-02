/*
 * Copyright (c) 2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.server.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sap.engine.services.webservices.espbase.wsdl.wsdl11.WSDL11Constants;
import com.sap.tc.logging.Location;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2006, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2006-2-16
 */
public class WSDLZipBuilder {
  
  void writeZipContent(String serviceWSDLUrl, WSDLVisualizer visualizer, OutputStream out) throws ServerRuntimeProcessException {
    DummyHttpServletRequest req = new DummyHttpServletRequest(serviceWSDLUrl);
    DummyHttpServletResponse resp = new DummyHttpServletResponse();
    //load service wsdl
    visualizer.writeWSDL0(req, resp);
    ZipOutputStream zip = new ZipOutputStream(out);
    //save the wsdls
    saveWSDL(zip, visualizer, resp.wsdl, new ArrayList());
    try {
      zip.finish();
    } catch (Exception e) {
      throw new ServerRuntimeProcessException(e);
    }
  }
  
  private String getWSDLName(List ids) {
    String wsdlName;
    if (ids.size() == 0) { //this is main wsdl
      wsdlName = "main.wsdl";
    } else {
      wsdlName = "file" + ids.size() + ".wsdl"; 
    }
    return wsdlName;
  }
  
  private String getWSDLName(List ids, String location) {
    int i = ids.indexOf(location);
    return "file" + (i + 1) + ".wsdl"; 
  }
  
  private void saveWSDL(ZipOutputStream zip, WSDLVisualizer visualizer, Element wsdl, List ids) throws ServerRuntimeProcessException{
    //store file name here because of the recursion below...
    String wsdlName = getWSDLName(ids);

    NodeList children = wsdl.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
        Element importElem = (Element) children.item(i);
        if (WSDL11Constants.WSDL_NS.equals(importElem.getNamespaceURI()) && WSDL11Constants.IMPORT_ELEMENT.equals(importElem.getLocalName())) {
          String importedWDSL = importElem.getAttribute("location");
          String iFName = null;
          if (! ids.contains(importedWDSL)) {
            //load the imported wsdl
            DummyHttpServletRequest req = new DummyHttpServletRequest(importedWDSL);
            DummyHttpServletResponse resp = new DummyHttpServletResponse();
            visualizer.writeWSDL0(req, resp);
            ids.add(importedWDSL); //increase the size of the list
            iFName = getWSDLName(ids);
            //process the imported wsdls
            saveWSDL(zip, visualizer, resp.wsdl, ids);
          } else {
            iFName = getWSDLName(ids, importedWDSL);
          }
          //modify the location to local url
          importElem.setAttribute("location", iFName);          
        }
      }
    }
    ZipEntry ze = new ZipEntry(wsdlName);
    try {
      zip.putNextEntry(ze);
      visualizer.writeDomToStream(wsdl, zip);
    } catch(Exception e) {
      throw new ServerRuntimeProcessException(e);
    }
  }
  
  private static class DummyHttpServletRequest implements HttpServletRequest {
    static final Location LOC = Location.getLocation(DummyHttpServletRequest.class);
    private URI uri;
    
    public DummyHttpServletRequest(String url) {
      LOC.debugT("constructor called with '" + url + "'");      
      uri = URI.create(url);
    }
    
    public String getRequestURI() {
      return uri.getRawPath(); //return the undecoded string, as it is how it is used by our runtime.
    }
    
    public String getParameter(String arg0) {
      String s = uri.getQuery();
      //parse query to extract parameter value.
      int start = -1;
      int index = -1;
      do{
        index = s.indexOf(arg0, index + 1);
        if (index < 0){
          return null;
        }
        if (index == 0 || s.charAt(index - 1) == '&' || s.charAt(index - 1) == '?'){ // found parameter
          start = index;
          break;
        }
      }while(index > -1);
      if (start < 0){ // not found
        return null;
      }
      int len = arg0.length();
      int last = s.indexOf('&', start + len);
      if (last == -1) {
        last = s.length();
      } 
      String res = s.substring(start + len, last);
      //exclude the leading '='
      if (res.length() == 1 && res.charAt(0) == '=') { 
        return "";
      } else if (res.length() > 1) {
        return res.substring(1);
      } else {
        return res;
      }
    }

    public String getScheme() {
      return uri.getScheme();
    }
    public String getServerName() {
      return uri.getHost();
    }
    public int getServerPort() {
      return uri.getPort();
    }

    public String getAuthType() {
      throw new UnsupportedOperationException();
    }
    public String getContextPath() {
      throw new UnsupportedOperationException();
    }
    public Cookie[] getCookies() {
      throw new UnsupportedOperationException();
    }
    public long getDateHeader(String arg0) {
      throw new UnsupportedOperationException();
    }
    public String getHeader(String arg0) {
      throw new UnsupportedOperationException();
    }
    public Enumeration getHeaderNames() {
      throw new UnsupportedOperationException();
    }
    public Enumeration getHeaders(String arg0) {
      throw new UnsupportedOperationException();
    }
    public int getIntHeader(String arg0) {
      throw new UnsupportedOperationException();
    }
    public String getMethod() {
      throw new UnsupportedOperationException();
    }
    public String getPathInfo() {
      throw new UnsupportedOperationException();
    }
    public String getPathTranslated() {
      throw new UnsupportedOperationException();
    }
    public String getQueryString() {
      throw new UnsupportedOperationException();
    }
    public String getRemoteUser() {
      throw new UnsupportedOperationException();
    }
    public String getRequestedSessionId() {
      throw new UnsupportedOperationException();
    }
    public StringBuffer getRequestURL() {
      throw new UnsupportedOperationException();
    }
    public String getServletPath() {
      throw new UnsupportedOperationException();
    }
    public HttpSession getSession() {
      throw new UnsupportedOperationException();
    }
    public HttpSession getSession(boolean arg0) {
      throw new UnsupportedOperationException();
    }
    public Principal getUserPrincipal() {
      throw new UnsupportedOperationException();
    }
    public boolean isRequestedSessionIdFromCookie() {
      throw new UnsupportedOperationException();
    }
    public boolean isRequestedSessionIdFromUrl() {
      throw new UnsupportedOperationException();
    }
    public boolean isRequestedSessionIdFromURL() {
      throw new UnsupportedOperationException();
    }
    public boolean isRequestedSessionIdValid() {
      throw new UnsupportedOperationException();
    }
    public boolean isUserInRole(String arg0) {
      throw new UnsupportedOperationException();
    }
    public Object getAttribute(String arg0) {
      throw new UnsupportedOperationException();
    }
    public Enumeration getAttributeNames() {
      throw new UnsupportedOperationException();
    }
    public String getCharacterEncoding() {
      throw new UnsupportedOperationException();
    }
    public int getContentLength() {
      throw new UnsupportedOperationException();
    }
    public String getContentType() {
      throw new UnsupportedOperationException();
    }
    public ServletInputStream getInputStream() throws IOException {
      throw new UnsupportedOperationException();
    }
    public String getLocalAddr() {
      throw new UnsupportedOperationException();
    }
    public Locale getLocale() {
      throw new UnsupportedOperationException();
    }
    public Enumeration getLocales() {
      throw new UnsupportedOperationException();
    }
    public String getLocalName() {
      throw new UnsupportedOperationException();
    }
    public int getLocalPort() {
      throw new UnsupportedOperationException();
    }
    
    public Map getParameterMap() {
      throw new UnsupportedOperationException();
    }
    public Enumeration getParameterNames() {
      throw new UnsupportedOperationException();
    }
    public String[] getParameterValues(String arg0) {
      throw new UnsupportedOperationException();
    }
    public String getProtocol() {
      throw new UnsupportedOperationException();
    }
    public BufferedReader getReader() throws IOException {
      throw new UnsupportedOperationException();
    }
    public String getRealPath(String arg0) {
      throw new UnsupportedOperationException();
    }
    public String getRemoteAddr() {
      throw new UnsupportedOperationException();
    }
    public String getRemoteHost() {
      throw new UnsupportedOperationException();
    }
    public int getRemotePort() {
      throw new UnsupportedOperationException();
    }
    public RequestDispatcher getRequestDispatcher(String arg0) {
      throw new UnsupportedOperationException();
    }
    
    public boolean isSecure() {
      throw new UnsupportedOperationException();
    }
    public void removeAttribute(String arg0) {
      throw new UnsupportedOperationException();
    }
    public void setAttribute(String arg0, Object arg1) {
      throw new UnsupportedOperationException();
    }
    public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
      throw new UnsupportedOperationException();
    }
}
  
  static class DummyHttpServletResponse implements HttpServletResponse {
    
    public Element wsdl;
    
    public void addCookie(Cookie arg0) {
    }
    public void addDateHeader(String arg0, long arg1) {
    }
    public void addHeader(String arg0, String arg1) {
    }
    public void addIntHeader(String arg0, int arg1) {
    }
    public boolean containsHeader(String arg0) {
      return false;
    }
    public String encodeRedirectUrl(String arg0) {
      return null;
    }
    public String encodeRedirectURL(String arg0) {
      return null;
    }
    public String encodeUrl(String arg0) {
      return null;
    }
    public String encodeURL(String arg0) {
      return null;
    }
    public void sendError(int arg0, String arg1) throws IOException {
    }
    public void sendError(int arg0) throws IOException {
    }
    public void sendRedirect(String arg0) throws IOException {
    }
    public void setDateHeader(String arg0, long arg1) {
    }
    public void setHeader(String arg0, String arg1) {
    }
    public void setIntHeader(String arg0, int arg1) {
    }
    public void setStatus(int arg0, String arg1) {
    }
    public void setStatus(int arg0) {
    }
    public void flushBuffer() throws IOException {
    }
    public int getBufferSize() {
      return 0;
    }
    public String getCharacterEncoding() {
      return null;
    }
    public String getContentType() {
      return null;
    }
    public Locale getLocale() {
      return null;
    }
    public ServletOutputStream getOutputStream() throws IOException {
      return null;
    }
    public PrintWriter getWriter() throws IOException {
      return null;
    }
    public boolean isCommitted() {
      return false;
    }
    public void reset() {
    }
    public void resetBuffer() {
    }
    public void setBufferSize(int arg0) {
    }
    public void setCharacterEncoding(String arg0) {
    }
    public void setContentLength(int arg0) {
    }
    public void setContentType(String arg0) {
    }
    public void setLocale(Locale arg0) {
    }
}
  
  
//  public static void main(String[] args) {
//    WSDLZipBuilder.DummyHttpServletRequest r = new WSDLZipBuilder.DummyHttpServletRequest("http://localhost:57200/SessionWS/SessionConfig?wsdl&style=&");
//    System.out.println(r.getRequestURI());
//    System.out.println(r.getParameter("wsdl"));
//    System.out.println(r.getParameter("style"));
//    System.out.println(r.getServerName());
//    System.out.println(r.getServerPort());
//    System.out.println(r.getScheme());
//  }
}
