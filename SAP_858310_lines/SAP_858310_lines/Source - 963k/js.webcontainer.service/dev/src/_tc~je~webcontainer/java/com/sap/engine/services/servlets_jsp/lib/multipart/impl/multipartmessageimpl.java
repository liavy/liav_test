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
package com.sap.engine.services.servlets_jsp.lib.multipart.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import com.sap.engine.services.httpserver.lib.util.ByteArrayUtils;
import com.sap.engine.services.servlets_jsp.lib.multipart.MultipartMessage;
import com.sap.engine.services.servlets_jsp.lib.multipart.MultipartParseException;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.runtime.client.HttpServletRequestFacade;

public class MultipartMessageImpl extends MultipartMessage {
  private static final String BOUNDARY_DELIMITER = "--";
  private static final String boundary_key = "boundary";
  private static final int MAX_HEADER_LENGTH = 4096;
  private static final int MAX_MEMORY_BODY = 32*1024;
  private static final int IO_BUFF_SIZE = 32*1024;
  private InputStream in = null;
  private String boundary = null;
  private byte[] boundary_ = null;
  private String contentTypeHeaderValue = null;
  private boolean finalBoundaryFound = false;
  private HttpServletRequestFacade request = null;
  
  // Helper variables for parseBody() method
  private boolean inMemory;
  private byte[] bodyBytes;
  private File bodyFile;

  public MultipartMessageImpl() {
    super();
  }

  public MultipartMessageImpl(InputStream in, String contentTypeHeaderValue, HttpServletRequestFacade request) {
    this();
    this.in = in;
    this.contentTypeHeaderValue = contentTypeHeaderValue;
    this.request = request;
    this.charset = null;
  }

  public void writeTo(OutputStream outputstream) throws IOException {

  }

  public void addFormParametersToRequest() {
    this.addFormParameters = true;
    request.parametersParsed = false;
  }  

  public void clear() {
    if (parts == null || parts.size() == 0) {
      // Not parsed or nothing to clear
      return;
    }
    
    Iterator iter = parts.iterator();
    while(iter.hasNext()) {
      MultipartPartImpl part = (MultipartPartImpl)iter.next();
      if (!part.isFormParameter() && !part.isInMemory()) {
          part.clear();
      }
    }
  }

  protected void parse() throws IOException, MultipartParseException {
    if (in == null) {
      return;
    }
    parseBoundary(contentTypeHeaderValue);
    findFirstBoundary();
    while (!finalBoundaryFound) {
      Hashtable headers = parseHeaders();
      inMemory = true; bodyBytes = null; bodyFile = null;
      parseBody();
      if (inMemory) {
        addBodyPart(new MultipartPartImpl(headers, bodyBytes));
      } else {
        addBodyPart(new MultipartPartImpl(headers, bodyFile));
      }
    }
  }

  // ------------------------ PRIVATE ------------------------
  /*
   * media-type     = type "/" subtype *( ";" parameter )
   *    type           = token
   *    subtype        = token
   */
  private void parseBoundary(String contentTypeHeaderValue) throws MultipartParseException {
    if (contentTypeHeaderValue == null) {
      throw new MultipartParseException(MultipartParseException.CONTENT_TYPE_HEADER_NOT_FOUND);
    }
    int semiColonInd = contentTypeHeaderValue.indexOf(';');
    if (semiColonInd == -1 || semiColonInd == contentTypeHeaderValue.length() - 1) {
      throw new MultipartParseException(MultipartParseException.BOUNDARY_NOT_FOUND);
    }
    contentType = contentTypeHeaderValue.substring(0, semiColonInd);
    contentTypeHeaderValue = contentTypeHeaderValue.substring(semiColonInd + 1);
    StringTokenizer params = new StringTokenizer(contentTypeHeaderValue, ";");
    while (params.hasMoreTokens()) {
      String param = params.nextToken().trim();
      int eqInd = param.indexOf('=');
      if (eqInd > -1 && eqInd < param.length() - 1) {
        String name = param.substring(0, eqInd);
        if (boundary_key.equals(name)) {
          boundary = param.substring(eqInd + 1).trim();
          if (boundary.length() > 1 && boundary.charAt(0) == '"' && boundary.charAt(boundary.length() - 1) == '"') {
            boundary = boundary.substring(1, boundary.length() - 1);
          }
          boundary = BOUNDARY_DELIMITER + boundary;
          boundary_ = boundary.getBytes();
          break;
        }
      }
    }
    if (boundary == null) {
      throw new MultipartParseException(MultipartParseException.BOUNDARY_NOT_FOUND);
    }
  }

  private void findFirstBoundary() throws IOException, MultipartParseException {
    byte[] nextLine = readLine();
    while (nextLine != null) {
      nextLine = ByteArrayUtils.trim(nextLine);
      if (ByteArrayUtils.equalsBytes(nextLine, boundary_)) {
        return;
      }
      nextLine = readLine();
    }
    throw new MultipartParseException(MultipartParseException.START_BOUNDARY_NOT_FOUND);
  }

  private Hashtable parseHeaders() throws IOException, MultipartParseException {
    Vector headerLines = new Vector();
    byte[] nextLine = readLine();
    while (nextLine != null) {
      if (nextLine.length == 0) {
        break;
      }
      if (nextLine[0] == ' ' || nextLine[0] == '\t') {
        byte[] previousHeader = (byte[])headerLines.elementAt(headerLines.size() - 1);
        if (previousHeader == null) {
          headerLines.addElement(nextLine);
        } else {
          byte[] compositeHeader = new byte[previousHeader.length + nextLine.length - 1];
          System.arraycopy(previousHeader, 0, compositeHeader, 0, previousHeader.length);
          System.arraycopy(nextLine, 1, compositeHeader, previousHeader.length, nextLine.length - 1);
          headerLines.addElement(compositeHeader);
        }
      } else {
        headerLines.addElement(nextLine);
      }
      nextLine = readLine();
    }
    Hashtable headers = new Hashtable();
    Enumeration en = headerLines.elements();
    while (en.hasMoreElements()) {
      byte[] next = (byte[])en.nextElement();
      int eqInd = ByteArrayUtils.indexOf(next, (byte)':');
      if (eqInd < 0 || eqInd >= next.length - 1) {
        throw new MultipartParseException(MultipartParseException.HEADER_VALUE_NOT_FOUND, new Object[]{new String(next)});
      }
      String name = null;
      String value = null;
      if (charset == null) {
        name = new String(next, 0, eqInd).trim();
        value = new String(next, eqInd + 1, next.length - eqInd - 1).trim();
      } else {
        name = new String(next, 0, eqInd, charset).trim();
        value = new String(next, eqInd + 1, next.length - eqInd - 1, charset).trim();
      }
      String[] oldValues = (String[])headers.get(name);
      if (oldValues == null) {
        headers.put(name, new String[]{value});
      } else {
        String[] newValues = new String[oldValues.length + 1];
        System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
        newValues[newValues.length - 1] = value;
        headers.put(name, newValues);
      }
    }
    return headers;
  }

  /*
   * Reads till
   * 1) the end of the stream - throws an exception
   * 2) the next boundary delimiter - ok + skips it
   * 3) the final boundary delimiter - ok + skips it + finalBoundaryFiund = true
   */
  private void parseBody() throws IOException, MultipartParseException {
    OutputStream out = new ByteArrayOutputStream();
    int nextByte = -1;
    while ((nextByte = in.read()) != -1) {
      if (inMemory && ((ByteArrayOutputStream)out).size() > MAX_MEMORY_BODY) {
        out = switchOutputStream((ByteArrayOutputStream)out);
      }
      if (nextByte == '\r') {
        in.mark(boundary_.length + 2);
        if (in.read() != '\n' || in.read() != '-') {
          in.reset();
          out.write(nextByte);
          continue;
        }
        int i = 1;
        for (; i < boundary_.length; i++) {
          if (in.read() != boundary_[i]) {
            in.reset();
            //out.write(nextByte);
            break;
          }
        }
        if (i == boundary_.length) {
          //a boundary delimiter found
          in.reset();
          in.read();
          in.read();
          for (int j = 0; j < boundary_.length - 1; j++) {
            in.read();
          }
          in.mark(3);
          if (in.read() == '-' && in.read() == '-') {
            in.reset();
            in.read();
            in.read();
            finalBoundaryFound = true;
          }
          in.reset();
          int tmp = in.read();
          while (tmp != -1 && tmp != '\n') {
            tmp = in.read();
          }
          if (inMemory) {
            bodyBytes = ((ByteArrayOutputStream)out).toByteArray();
          } else {
            out.close();
          }
          return;
        }
        in.reset();
      }
      out.write(nextByte);
    }
    throw new MultipartParseException(MultipartParseException.FINAL_BOUNDARY_NOT_FOUND);
  }

  /*
   * Returns the next line WITHOUT the \r\n symbols.
   */
  private byte[] readLine() throws IOException {
    int nextByte = -1;
    byte[] line = null;
    in.mark(MAX_HEADER_LENGTH);
    if ((nextByte = in.read()) != -1) {
      int len = 1;
      int prev = -1;
      while (nextByte != -1 && nextByte != '\n') {
        prev = nextByte;
        nextByte = in.read();
        len++;
      }
      in.reset();
      if (prev == '\r') {
        line = new byte[len - 2];
      } else {
        line = new byte[len - 1];
      }
      in.read(line);
      if (prev == '\r') {
        in.read();
      }
      in.read();
      return line;
    }
    return null;
  }
  
  private OutputStream switchOutputStream(ByteArrayOutputStream out) 
      throws IOException {
    inMemory = false;
    File tempDir = new File(ServiceContext.getServiceContext().getTempDirectory());
    if (!tempDir.exists()) { tempDir.mkdir(); }
    bodyFile = File.createTempFile("part", ".temp", tempDir);
    FileOutputStream fout = new FileOutputStream (bodyFile);
    out.writeTo(fout);
    // By setting up such an output stream, an application can write bytes
    // to underlying output stream without necessarily causing a call to the 
    // the underlying system for each byte written
    return new BufferedOutputStream(fout, IO_BUFF_SIZE);
  }
}
