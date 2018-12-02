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
package com.sap.engine.services.servlets_jsp.server.servlet;

import java.io.*;
import java.util.TimeZone;
import java.util.Locale;
import javax.servlet.*;
import javax.servlet.http.*;
import com.sap.engine.lib.util.ArrayObject;
import com.sap.engine.lib.text.FastDateFormat;
import com.sap.engine.services.httpserver.lib.util.MessageBytes;
import com.sap.engine.services.httpserver.lib.util.ByteArrayUtils;
import com.sap.engine.services.httpserver.lib.*;
import com.sap.engine.services.httpserver.lib.protocol.HeaderNames;
import com.sap.engine.services.httpserver.interfaces.HttpParameters;
import com.sap.engine.services.httpserver.interfaces.properties.HostProperties;
import com.sap.engine.services.servlets_jsp.server.*;
import com.sap.engine.services.servlets_jsp.server.lib.FilterUtils;
import com.sap.engine.services.servlets_jsp.server.runtime.client.HttpServletRequestFacade;
import com.sap.engine.services.servlets_jsp.server.runtime.client.HttpServletResponseFacade;
import com.sap.engine.services.servlets_jsp.server.runtime.ServletConfigImpl;
import com.sap.engine.services.servlets_jsp.server.application.ServletContextImpl;


public class DefaultServlet extends HttpServlet {
  private static final Locale loc = Locale.US;
  private static final TimeZone localZone = TimeZone.getDefault();
  private static final FastDateFormat listFormat = new FastDateFormat("dd-MMM-yyyy HH:mm", localZone, loc, false);

  private ServletConfigImpl servletConfig = null;

  public void init(ServletConfig servletConfig) throws ServletException {
    this.servletConfig = (ServletConfigImpl)servletConfig;
    super.init(servletConfig);
  }

  public void service(HttpServletRequest httpservletrequest, HttpServletResponse httpservletresponse) throws ServletException, IOException {
    HttpServletRequestFacade requestUnwrapped = FilterUtils.unWrapRequest(httpservletrequest);
    HostProperties hostProperties = ServiceContext.getServiceContext().getHttpProvider().getHostProperties(requestUnwrapped.getHttpParameters().getHostName());
    String requestURI = null;
    if (requestUnwrapped.getHttpParameters().getRequest().getRequestLine().isEncoded()) {
      requestURI = requestUnwrapped.getHttpParameters().getRequest().getRequestLine().getUrlDecoded().toStringUTF8();
    } else {
      requestURI = requestUnwrapped.getHttpParameters().getRequest().getRequestLine().getUrlDecoded().toString();
    }
    requestURI = new String(ParseUtils.separatorsToSlash(requestURI));
    MessageBytes aliasName = requestUnwrapped.getHttpParameters().getRequestPathMappings().getAliasName();
    aliasName = new MessageBytes(ParseUtils.separatorsToSlash(aliasName.toString()));
    if (!aliasName.equals(ParseUtils.separator)) {
      if (requestURI.startsWith(ParseUtils.separator + aliasName.toString())) {
        requestURI = requestURI.substring(aliasName.length() + 1);
        if (!requestURI.startsWith(ParseUtils.separator)) {
          requestURI = ParseUtils.separator + requestURI;
        }
      }
    }
    String aliasValue = requestUnwrapped.getHttpParameters().getRequestPathMappings().getAliasValue();
    if (aliasValue == null) {
      FilterUtils.unWrapResponse(httpservletresponse).sendError(ResponseCodes.code_internal_server_error, Responses.mess36);//here we do not need user action
      return;
    }
    aliasValue = new String(ParseUtils.separatorsToSlash(aliasValue));
    String realPath = aliasValue + requestURI;
    realPath = ParseUtils.canonicalizeFS(realPath);
    File f = new File(realPath);
    //The check when f.exists()is added because of CSN 1405775 2009 - JSP security vulnerability 
    //If there is a trailing slash in the initial request, it is not taken into account here due to the cannonicalizeFS method that removes it 
    //without this additional check (after the logical or || below) a download of an app file (with path realPath) may be enabled as a side effect if there is such file
      if (!f.exists() || (requestURI.endsWith(ParseUtils.separator) && !f.isDirectory())) {
      httpservletresponse.sendError(ResponseCodes.code_not_found, Responses.mess37.replace("{REQUESTURI}", requestURI));//here we do not need user action
      return;
    }
    String canonicalPath = new String(ParseUtils.separatorsToFS(f.getCanonicalPath()));
    byte[] canonicalPathBytes = canonicalPath.getBytes();
    if (!new String(ParseUtils.separatorsToSlash(canonicalPath)).startsWith(aliasValue)) {
      httpservletresponse.sendError(ResponseCodes.code_not_found, Responses.mess37.replace("{REQUESTURI}", requestURI));//here we do not need user action
      return;
    }
    if (!ByteArrayUtils.equalsBytes(canonicalPathBytes, realPath.getBytes())) {
      httpservletresponse.sendError(ResponseCodes.code_not_found, Responses.mess37.replace("{REQUESTURI}", requestURI));//here we do not need user action
      return;
    }
    if (!aliasValue.endsWith(ParseUtils.separator)) {
      aliasValue = aliasValue + ParseUtils.separator;
    }
    if (ByteArrayUtils.startsWithIgnoreCase(canonicalPathBytes, ParseUtils.separatorsToFS(aliasValue + "META-INF"))
        || ByteArrayUtils.startsWithIgnoreCase(canonicalPathBytes,  ParseUtils.separatorsToFS(aliasValue + "WEB-INF"))) {
      httpservletresponse.sendError(ResponseCodes.code_not_found, Responses.mess38);//here we do not need user action
      return;
    }

    //set cache headers - Cache-Control and/or Expires
    int httpMajorVersion = requestUnwrapped.getHttpParameters().getRequest().getRequestLine().getHttpMajorVersion();
    int minorVersion = requestUnwrapped.getHttpParameters().getRequest().getRequestLine().getHttpMinorVersion();
    FilterUtils.addCacheHeaders(httpservletresponse, httpMajorVersion, minorVersion);
    
    //set last-modified header for the returned file
    if (checkIfModifiedSince(f, requestUnwrapped)){
    	httpservletresponse.addDateHeader(HeaderNames.entity_header_last_modified, f.lastModified());
    }else{
    	httpservletresponse.setStatus(ResponseCodes.code_not_modified);
    }
    
    //ako e file go vrashtame
    if (!f.isDirectory()) {
      returnFile(f, httpservletresponse, httpservletrequest);
    } else if (f.isDirectory()) {
      String reqURI = ParseUtils.separator + aliasName + requestURI;
      if (!reqURI.endsWith(ParseUtils.separator)) {
        httpservletresponse.sendRedirect(reqURI + ParseUtils.separator);
        return;
      }
      returnDirectory(f, reqURI, realPath, hostProperties, requestUnwrapped, httpservletresponse, hostProperties);
    }
  }

  // ------------------------ PRIVATE ------------------------
  /**
   * Check if the given file has been modified since the date specified in the If-modified-since 
   * header of the given request
   * @return        true if it is modified or if there is no If-modified-since header
   */
  private boolean checkIfModifiedSince(File f, HttpServletRequestFacade requestUnwrapped) {
	long date = requestUnwrapped.getHttpParameters().getRequest().getHeaders().getDateHeader(HeaderNames.request_header_if_modified_since_); 
    if (date != -1) {
      return f.lastModified() > date ;
    }
    return true;
  }
  
  private void returnFile(File f, HttpServletResponse httpservletresponse, HttpServletRequest httpservletrequest) throws IOException {
    String type = ((ServletContextImpl) servletConfig.getServletContext())
      .getApplicationContext().getServletContext().getMimeType(f.getName());
    if (type == null) {
      type = f.getName();
      int i = type.indexOf(".");
      if (i > 0) {
        type = type.substring(i);
        // MimeMappings.getMimeType(..) method requires passed
        // argument to include dot + extension (example: .txt)
        type = ServiceContext.getServiceContext().getHttpProvider()
          .getHttpProperties().getMimeMappings().getMimeType(type);
      }
    }

    if (type != null) {
      httpservletresponse.setContentType(type.replace('/', ParseUtils.separatorChar).replace('\\', ParseUtils.separatorChar));
    } else {
      httpservletresponse.setContentType("content/unknown");
    }

    ServletOutputStream servletOutputStream = null;
    PrintWriter printWriter = null;
    try {
      servletOutputStream = httpservletresponse.getOutputStream();
    } catch (IllegalStateException ie) {
      if (type.startsWith("text")) { //todo i za xml, xsl
        printWriter = httpservletresponse.getWriter();
      } else {
        throw ie;
      }
    }

    if (servletOutputStream != null) {
      writeFile(f, servletOutputStream, httpservletrequest, httpservletresponse);
    } else {
      writeFile(f, printWriter, httpservletrequest, httpservletresponse);
    }
  }

  private void returnDirectory(File f, String reqURI, String realPath, HostProperties desc,
                               HttpServletRequestFacade requestUnwrapped, HttpServletResponse httpservletresponse,
                               HostProperties hostProperties) throws IOException {
    //welcome files
    ArrayObject welcomeFiles = ((ServletContextImpl) servletConfig.getServletContext()).getApplicationContext().getWebApplicationConfiguration().getWelcomeFiles();
    for (int i = 0; i < welcomeFiles.size(); i++) {
    	byte[] welcomeFile = (byte[]) welcomeFiles.elementAt(i);
      String fileName = new String(welcomeFile);
      String redirectLocation = reqURI + fileName;

      if (new File(realPath + (realPath.endsWith(File.separator) ? "" : File.separator) + fileName).exists()) {
      	//redirect to welcome file
				httpservletresponse.sendRedirect(redirectLocation);
        return;
      }
    }
    //start page
    String startPage = desc.getStartPage();
    if (startPage != null && reqURI.equals(ParseUtils.separator) && !startPage.trim().equals("")) {
      if (new File(realPath + (realPath.endsWith(File.separator) ? "" : File.separator) + startPage).exists()) {
        httpservletresponse.sendRedirect(reqURI + startPage);
        return;
      }
    }
    //welcome files http
    String[] welcome = ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getInfernames();
    for (int i = 0; i < welcome.length; i++) {
      if (new File(realPath + (realPath.endsWith(File.separator) ? "" : File.separator) + welcome[i]).exists()) {
        httpservletresponse.sendRedirect(reqURI + welcome[i]);
        return;
      }
    }
    if (!desc.isList()) {
      httpservletresponse.sendError(ResponseCodes.code_forbidden, Responses.mess3);//here we do not need user action
      return;
    }
    ServletOutputStream servletOutputStream = null;
    PrintWriter printWriter = null;
    try {
      servletOutputStream = httpservletresponse.getOutputStream();
    } catch (IllegalStateException ie) {
      printWriter = httpservletresponse.getWriter();
    }
    listDirectory(f, servletOutputStream, printWriter, requestUnwrapped, hostProperties);
  }

  /**
   * Make listing of directory and send it.
   */
  private void listDirectory(File file, ServletOutputStream outStream, PrintWriter printWriter,
                             HttpServletRequestFacade requestUnwrapped, HostProperties hostProperties) throws IOException {
    HttpParameters request = requestUnwrapped.getHttpParameters();
    File[] files = file.listFiles();
    boolean flag = false;
    if (request.getRequestPathMappings().getAliasName() != null) {
      //ConcurrentHashMapObjectObject alias = request.getDescriptor().getAliases();
      String pth = hostProperties.getAliasValue(request.getRequestPathMappings().getAliasName().toString());
      if (pth != null) {
        if (pth.equals(file.toString().replace(File.separatorChar, ParseUtils.separatorChar))) {
          flag = true;
        }
      }
    } else {
      if (hostProperties.getRootDir().equals(file.toString().replace(File.separatorChar, ParseUtils.separatorChar))) {
        flag = true;
      }
    }
    String parrentDirectory = requestUnwrapped.getRequestURI();
    byte[] tmpBytes = Responses.getDirectoryHead(ServiceContext.getServiceContext().getServerVersion(),
        requestUnwrapped.getRequestURI(), (flag ? null :  parrentDirectory.substring(0, parrentDirectory.substring(0, parrentDirectory.length() - 1).lastIndexOf(ParseUtils.separatorChar) + 1)));
    writeDirectoryList(outStream, printWriter, tmpBytes);
    for (int i = 0; i < files.length; i++) {
      if (flag) {
        if (files[i].getName().equalsIgnoreCase("META-INF") || files[i].getName().equalsIgnoreCase("WEB-INF")) {
          continue;
        }
      }
      byte[] dateArr = new byte[17];
      listFormat.getDate(dateArr, 0, files[i].lastModified());
      if (files[i].isDirectory()) {
        tmpBytes = Responses.getDirectoryLine(files[i].getName(), new String(dateArr));
      } else {
        int size = (int) files[i].length();
        if (size == 0 ) {
          tmpBytes = Responses.getFileLine(files[i].getName(), Responses.getZero() , new String(dateArr));
        } else if (size < 1024) {
          tmpBytes = Responses.getFileLine(files[i].getName(), (size / 100 > 0 ? "0." + size / 100 : "0.1" ) , new String(dateArr));
        } else {
          size = size / 1024;
          tmpBytes = Responses.getFileLine(files[i].getName(), "" + size , new String(dateArr));
        }
      }
      writeDirectoryList(outStream, printWriter, tmpBytes);
    }
    writeDirectoryList(outStream, printWriter, Responses.getTableEnd());
  }

  private void writeDirectoryList(ServletOutputStream outStream, PrintWriter printWriter, byte[] messByte) throws IOException {
    if (outStream != null) {
      outStream.write(messByte);
      outStream.flush();
    } else {
      printWriter.write(new String(messByte));
      printWriter.flush();
    }
  }

  private void writeFile(File f, Writer out, ServletRequest servletrequest, ServletResponse servletresponse) throws IOException {
    int read = 0;
    int[] range;
    char[] buf = new char[4096];

    range = prepareWriteFile(f, servletrequest, servletresponse);
    if (range != null) {
      int start = range[0];
      int finish = range[1];
      long length = finish - start + 1;;
      byte[] srcBuf = new byte[buf.length];
      RandomAccessFile r = new RandomAccessFile(f, "r");
      try {
        r.seek(start);
        while (length > 0 && (read = r.read(srcBuf, 0, (length > srcBuf.length) ? srcBuf.length : (int) length)) != -1) {
          for (int i = 0; i < read; i++) {
            buf[i] = (char) (srcBuf[i] & 0xFF);
          }
          out.write(buf, 0, read);
          length = length - read;
        }
      } finally {
        r.close();
      }
    } else {
      InputStreamReader in = new InputStreamReader(new FileInputStream(f));
      try {
        while ((read = in.read(buf)) != -1) {
          out.write(buf, 0, read);
        }
      } finally {
        in.close();
      }
    }
  }

  private void writeFile(File f, OutputStream out, ServletRequest servletrequest, ServletResponse servletresponse) throws IOException {
    int read = 0;
    int[] range;
    byte[] srcBuf = new byte[4096];

    range = prepareWriteFile(f, servletrequest, servletresponse);
    if (range != null) {
      int start = range[0];
      int finish = range[1];
      long length = finish - start + 1;;
      RandomAccessFile r = new RandomAccessFile(f, "r");
      try {
        r.seek(start);
        while (length > 0 && (read = r.read(srcBuf, 0, (length > srcBuf.length) ? srcBuf.length : (int) length)) != -1) {
          out.write(srcBuf, 0, read);
          length = length - read;
        }
      } finally {
        r.close();
      }
    } else {
      FileInputStream in = new FileInputStream(f);
      try {
        while ((read = in.read(srcBuf)) != -1) {
          out.write(srcBuf, 0, read);
        }
      } finally {
        in.close();
      }
    }
  }

  private int[] prepareWriteFile(File f, ServletRequest servletrequest, ServletResponse servletresponse) {
    String s = FilterUtils.unWrapRequest(servletrequest).getHeader(HeaderNames.request_header_range);

    if (s != null) {
      s = s.toUpperCase();
      if (s.indexOf("BYTES") > -1) {
        int start = -1;
        int finish = -1;
        long length;
        String s1;
        String s2;
        int flen = (int) f.length();

        s = s.substring(s.indexOf("=") + 1).trim();
        int i = s.indexOf("-");
        s1 = s.substring(0, i);
        s2 = s.substring(i + 1);
        if (i == 0) {
          finish = flen - 1;
          start = finish - (new Integer(s2)).intValue();
        } else if (i == (s.length() - 1)) {
          start = (new Integer(s1)).intValue();
          finish = flen - 1;
        } else {
          start = (new Integer(s1)).intValue();
          finish = (new Integer(s2)).intValue();
        }

        if (finish > flen) {
          finish = flen - 1;
        }

        if (start <= finish) {
          HttpServletResponseFacade response = FilterUtils.unWrapResponse(servletresponse);
          response.setStatus(ResponseCodes.code_partial_content);
          response.setHeader(HeaderNames.entity_header_content_range, "bytes " + start + "-" + finish + "/" + flen);
          
          if (start > 0 || finish < flen - 1) {
            length = finish - start + 1;
            response.setIntHeader(HeaderNames.entity_header_content_length, (int)length);
            return new int[] {start, finish};
          }
        }
      }
    }
    return null;
  }

}

