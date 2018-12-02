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
package com.sap.engine.services.servlets_jsp.lib.multipart;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * This class represents a sub-message of a multipart http message. Each sub-message has headers and body.
 *
 * @author Maria Jurova
 * @version 6.30
 * @see MultipartMessage
 */
public abstract class MultipartPart {
  private static final String form_data = "form-data";
  protected int size = -1;
  private String contentType = null;
  private String characterEncoding = null;
  private boolean contentTypeParsed = false;
  private String disposition = null;
  private boolean dispositionParsed = false;
  private String fileName = null;
  private String name = null;
  private boolean isFormParameter = false;
  private boolean isFormParameterParsed = false;
  private byte[] body = null;
  protected Hashtable headers = null;

  /**
   * Returns the size of the body of this multipart sub-message.
   *
   * @return the size of the sub-message body
   */
  public int getSize() {
    return size;
  }

  /**
   * Returns the content type of this multipart sub-message as specified by
   * its Content-Type headers
   *
   * @return the content type of the sub-message
   */
  public String getContentType() {
    if (!contentTypeParsed) {
      parseContentType();
      contentTypeParsed = true;
    }
    return contentType;
  }

  /**
   * Returns the content-disposition of this multipart sub-message.
   * The content-disposition is the value of the Content-Disposition header
   * (without its parameters) of the sub-message
   *
   * @return the value of the content-disposition header (without its parameters)
   * of this message 
   */
  public String getDisposition() {
    if (!dispositionParsed) {
      parseDisposition();
      dispositionParsed = true;
    }
    return disposition;
  }

  /**
   * Returns the name of the body of this multipart sub-message.
   * The name is the value of the parameter "name" of the Content-Disposition
   * header of this sub-message
   *
   * @return the name of this sub-message
   */
  public String getName() {
    if (!dispositionParsed) {
      parseDisposition();
      dispositionParsed = true;
    }
    return name;
  }

  /**
   * Returns if this multipart sub-message represents a form-based request
   * parameter as specified by RFC 1867.
   *
   * @return true, if this sub-message represents a form-based request parameter,
   * and false otherwise
   */
  public boolean isFormParameter() {
    if (!dispositionParsed) {
      parseDisposition();
      dispositionParsed = true;
    }
    if (!isFormParameterParsed) {
      parseIsFormParameter();
      isFormParameterParsed = true;
    }
    return isFormParameter;
  }

  /**
   * Returns the file name of the file that this sub-message represents.
   * The sub-message can represent a file if the http multipart message has the same syntax as the one defined by RFC 1867.
   *
   * @return  the name of the file this sub-message represents or null if the message doesn't represent a file as defined by RFC 1867
   */
  public String getFileName() {
    if (!dispositionParsed) {
      parseDisposition();
      dispositionParsed = true;
    }
    return fileName;
  }

  /**
   * Returns an input stream for reading the body of this
   * multipart sub-message
   *
   * @return an input stream for reading the body of this multipart
   * sub-message
   * 
   * @see getBody()
   * @see writeTo(java.io.File)
   */
  public abstract InputStream getInputStream();

  /**
   * Returns a byte array containing the body of this 
   * multipart sub-message
   * <p>
   * Use this method carefully, cause it tries to load 
   * the whole body in the memory
   * 
   * @return the body of this message represented as byte array. 
   * The size of the byte array is equal to the size of the body.
   * @throws IOException   if some error occurs while reading the input stream of the multipart sub-message
   */
  public byte[] getBody() throws IOException {
    if (body != null) {
      return body;
    }
    InputStream in = getInputStream();
    body = new byte[in.available()];
    int offset = 0;
    int read = -1;
    while (offset < body.length && (read = in.read(body, offset, body.length - offset)) != -1) {
      offset += read;
    }
    return body;
  }

  /**
   * Sets a body of this multipart sub-message.
   *
   * @param text   A String representing the sub-message body
   */
  public abstract void setText(String text);

  /**
   * Writes this multipart sub-message to an output stream according 
   * to the multipart message's syntax.
   *
   * @param outputstream
   * An <code>java.io.OutputStream</code> where the message will be written to
   */
  public abstract void writeTo(OutputStream outputstream);
  
  /**
   * Writes this multipart sub-message to an file according 
   * to the multipart message's syntax.
   * <p>
   * Method don't guarantee that more than one call will succeed 
   *
   * @param destonation an <code>java.io.File</code> where the message will be written to
   * @throws IOException   if some error occurs while writing to the outtput stream of the multipart sub-message
   */
  public abstract void writeTo(File destonation) throws IOException;

  /**
   * Returns the value of a header with a specified name that is set for this multipart sub-message.
   *
   * @param headerName   The name of the header
   * @return The value of the first header with the specified name that is set for this message, or null if such header does not exist
   */
  public String getHeader(String headerName) {
    String[] headerValues = getHeaderValues(headerName);
    if (headerValues == null || headerValues.length == 0) {
      return null;
    }
    return headerValues[0];
  }

  /**
   * Returns the values of headers with specified name of this multipart sub-message.
   *
   * @param      headerName   The name of the header 
   * @return     An array with the values of all headers with the specified name, or null if such headers do not exist
   */
  public String[] getHeaderValues(String headerName) {
    if (headers == null) {
      return null;
    }
    return (String[])headers.get(headerName);
  }

  /**
   * Sets a header with specified name and value in this multipart sub-message.
   * If the sub-message already contains headers with the same name, they will be removed.
   *
   * @param      headerName   The name of the header that will be set
   * @param      headerValue  The value of the header that will de set
   */
  public void setHeader(String headerName, String headerValue) {
    if (headers == null) {
      headers = new Hashtable(3, 3);
    }
    headers.put(headerName, new String[]{headerValue});
  }

  /**
   * Adds a header with specified name and value in this multipart sub-message.
   *
   * @param      headerName   The name of the header that will be added
   * @param      headerValue  The value of the header that will de added
   */
  public void addHeader(String headerName, String headerValue) {
    if (headers == null) {
      headers = new Hashtable(3, 3);
    }
    String[] oldHeaderValues = (String[])headers.get(headerName);
    if (oldHeaderValues == null || oldHeaderValues.length == 0) {
      headers.put(headerName, new String[]{headerValue});
    } else {
      String[] newHeaderValues = new String[oldHeaderValues.length + 1];
      System.arraycopy(oldHeaderValues, 0, newHeaderValues, 0, oldHeaderValues.length);
      newHeaderValues[newHeaderValues.length - 1] = headerValue;
      headers.put(headerName, newHeaderValues);
    }
  }

  /**
   * Removes a header with specified name from this multipart sub-message.
   *
   * @param      headerName   The name of the header that will be removed
   */
  public void removeHeader(String headerName) {
    if (headers != null) {
      headers.remove(headerName);
    }
  }

  /**
   * Returns an enumeration of the names of all headers of this multipart sub-message.
   * Each element of this enumeration is a String representing the name of the next header.
   *
   * @return      An enumeration of the names of all headers of this multipart sub-message
   */
  public Enumeration getAllHeaderNames() {
    return headers.keys();
  }

  /**
   * Returns the value of the character encoding of this sub-message as specified by its Content-type header
   *
   * @return      The value of the character encoding of this sub-message
   */
  public String getCharacterEncoding() {
    if (!contentTypeParsed) {
      parseContentType();
      contentTypeParsed = true;
    }
    return characterEncoding;
  }

  /**
   * Returns the value of the Content-Transfer-Encoding header of this sub-message
   *
   * @return      The value of the Content-Transfer-Encoding header of this sub-message
   */
  public String getTransferEncoding() {
    String s = getHeader("Content-Transfer-Encoding");
    if (s == null) {
      return null;
    }
    s = s.trim();
    if (s.equalsIgnoreCase("7bit") || s.equalsIgnoreCase("8bit")
        || s.equalsIgnoreCase("quoted-printable") || s.equalsIgnoreCase("base64")) {
      return s;
    }
    //todo - kakvo da se vyrne??
    return s;
  }

  /**
   * Returns the value of the Content-Id header of this sub-message
   *
   * @return      The value of the Content-Id header of this sub-message
   */
  public String getContentID() {
    return getHeader("Content-Id");
  }

  private void parseDisposition() {
    disposition = getHeader("Content-Disposition");
    if (disposition == null) {
      return;
    }
    disposition = disposition.trim();
    int scInd = disposition.indexOf(';');
    if (scInd == -1) {
      return;
    }
    if (scInd == disposition.length() - 1) {
      disposition = disposition.substring(0, scInd).trim();
      return;
    }
    String paramString = disposition.substring(scInd + 1).trim();
    disposition = disposition.substring(0, scInd).trim();
    StringTokenizer params = new StringTokenizer(paramString, ";");
    while (params.hasMoreTokens()) {
      String param = params.nextToken();
      int eqInd = param.indexOf('=');
      if (eqInd != -1 && eqInd < param.length() - 1) {
        String paramName = param.substring(0, eqInd).trim();
        String paramValue = param.substring(eqInd + 1).trim();
        if (paramName.equals("name")) {
          name = unquote(paramValue);
        } else if (paramName.equals("filename")) {
          fileName = unquote(paramValue);
        }
      }
    }
  }

  private void parseContentType() {
    contentType = getHeader("Content-Type");
    if (contentType != null) {
      int charsetLocation = contentType.indexOf("charset=");
      if (charsetLocation != -1) {
        int end = contentType.indexOf((byte)';', charsetLocation);
        if (end == -1) {
          end = contentType.length();
        }
        characterEncoding = contentType.substring(charsetLocation + 8, end - (charsetLocation + 8));
        characterEncoding = unquote(characterEncoding);
      }
    }
  }

  private void parseIsFormParameter() {
    isFormParameter = form_data.equalsIgnoreCase(getDisposition()) && getFileName() == null && getName() != null;
  }

  private String unquote(String str) {
    if (str.length() < 2) {
      return str;
    }
    if (str.startsWith("\"") && str.endsWith("\"")) {
      return str.substring(1, str.length() - 1);
    }
    if (str.startsWith("'") && str.endsWith("'")) {
      return str.substring(1, str.length() - 1);
    }
    return str;
  }
}
