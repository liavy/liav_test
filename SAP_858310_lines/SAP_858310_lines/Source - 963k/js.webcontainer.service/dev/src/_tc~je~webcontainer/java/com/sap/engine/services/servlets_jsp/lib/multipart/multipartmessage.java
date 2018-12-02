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

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Vector;

import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.lib.EmptyEnumeration;
import com.sap.tc.logging.Location;

/**
 * This class represents a multipart message body. An http message is multipart if it has content-type starting with
 * "multipart/". A multipart message consists of multipart sub-messages. This class is actually a set of all
 * sub-messages of a multipart message. It contains methods for getting the sub-messages count, obtaining a single message by name or index,
 * adding and removing multipart sub-messages.
 * A multipart request body can be obtained as instance of this class using the
 * getAttribute(String s) method of HttpServletRequest interface. The default parameter that must be passed to this method
 * is <tt>MULTIPART_BODY_KEY</tt>.
 *
 * @author Maria Jurova
 * @version 6.30
 */
public abstract class MultipartMessage {
  /**
   * The default key used for accessing a multipart request / response body through servlet API.
   */
  public static final String MULTIPART_BODY_KEY = "com.sap.servlet.multipart.body";
  /**
   * The content type used for submitting forms that contain files, non-ASCII data, and binary data in series of parts.
   */
  public static final String FORM_DATA_TYPE = "form-data";
  protected Vector parts = null;
  protected String contentType = null;
  private boolean parsed = false;
  protected boolean addFormParameters = false;
  protected String charset = null;
  private static Location traceLocation = LogContext.getLocationMultipart();
  /**
   * Returns the content type of this multipart body. It usually starts with "multipart/".
   *
   * @return     The content type of this multipart body
   * @exception     IOException   If some error occurs in reading / writing to the input stream of the message body
   * @exception     MultipartParseException   If the message body doesn't represent a valid multipart message
   */
  public String getContentType() throws IOException, MultipartParseException {
    ensureParsed();
    return contentType;
  }

  /**
   * Returns the number of the sub-messages within this multipart message.
   *
   * @return     The number of the sub-messages within this multipart message
   * @exception     IOException   If some error occurs in reading / writing to the input stream of the message body
   * @exception     MultipartParseException   If the message body doesn't represent a valid multipart message
   */
  public int getCount() throws IOException, MultipartParseException {
    ensureParsed();
    if (parts == null) {
      return 0;
    } else {
      return parts.size();
    }
  }

  /**
   * Returns an enumeration of all sub-messages within this multipart message.
   *
   * @return     An enumeration of all sub-messages within this multipart message.
   * @exception     IOException   If some error occurs in reading / writing to the input stream of the message body
   * @exception     MultipartParseException   If the message body doesn't represent a valid multipart message
   */
  public Enumeration getBodyParts() throws IOException, MultipartParseException {
    ensureParsed();
    if (parts == null) {
      return new EmptyEnumeration();
    } else {
      return parts.elements();
    }
  }

  /**
   * Returns a sub-message at a specified position within this multipart message.
   *
   * @param      i    The position of the sub-message that will be returned or null if a sub-message with such index does not exist
   * @return     The sub-message at position <tt>i</tt> within this multipart message
   * @exception     IOException   If some error occurs in reading / writing to the input stream of the message body
   * @exception     MultipartParseException   If the message body doesn't represent a valid multipart message
   * @exception  ArrayIndexOutOfBoundsException  if the <tt>i</tt> is a negative number or number that exceeds the number of sub-messages
   */
  public MultipartPart getBodyPart(int i) throws IOException, MultipartParseException {
    ensureParsed();
    if (parts == null) {
      throw new MultipartParseException(MultipartParseException.NO_SUCH_MULTIPART_PART);
    } else {
      return (MultipartPart)parts.elementAt(i);
    }
  }

  /**
   * Removes a sub-message from this multipart message.
   *
   * @param      bodypart    The sub-message that will be removed
   * @return     True, if such sub-message exists, or false otherwise
   * @exception     IOException   If some error occurs in reading / writing to the input stream of the message body
   * @exception     MultipartParseException   If the message body doesn't represent a valid multipart message
   */
  public boolean removeBodyPart(MultipartPart bodypart) throws IOException, MultipartParseException {
    ensureParsed();
    if (parts == null) {
      throw new MultipartParseException(MultipartParseException.NO_SUCH_MULTIPART_PART);
    } else {
      return parts.removeElement(bodypart);
    }
  }

  /**
   * Removes a sub-message on a specified position from this multipart message.
   *
   * @param      i    The index of the sub-message that will be removed
   * @exception     IOException   If some error occurs in reading / writing to the input stream of the message body
   * @exception     MultipartParseException   If the message body doesn't represent a valid multipart message
   * @exception  ArrayIndexOutOfBoundsException  if the index is either negative number of exceeds the number of sub-messages within this multipart message.
   */
  public void removeBodyPart(int i) throws IOException, MultipartParseException {
    ensureParsed();
    if (parts == null) {
      throw new MultipartParseException(MultipartParseException.NO_SUCH_MULTIPART_PART);
    } else {
      parts.removeElementAt(i);
      return;
    }
  }

  /**
   * Adds a sub-message to this multipart message. The new sub-message is added at the last position of the list of sub-messages.
   *
   * @param      bodypart    The sub-message that will be added
   * @exception     IOException   If some error occurs in reading / writing to the input stream of the message body
   * @exception     MultipartParseException   If the message body doesn't represent a valid multipart message
   */
  public void addBodyPart(MultipartPart bodypart) throws IOException, MultipartParseException {
    synchronized (this) {
      ensureParsed();
      if (parts == null) {
        parts = new Vector(3, 3);
      }
      parts.addElement(bodypart);
    }
  }

  /**
   * Adds a sub-message in this multipart message on a specified position.
   *
   * @param      bodypart    The sub-message that will be added
   * @param      i    The index, identifying the position on which the sub-message will be added. The first message has index 0.
   * @exception     IOException   If some error occurs in reading / writing to the input stream of the message body
   * @exception     MultipartParseException   If the message body doesn't represent a valid multipart message
   * @exception     ArrayIndexOutOfBoundsException  if the index is either negative number of exceeds the number of sub-messages within this multipart message.
   */
  public void addBodyPart(MultipartPart bodypart, int i) throws IOException, MultipartParseException {
    synchronized (this) {
      ensureParsed();
      if (parts == null) {
        parts = new Vector();
      }
      parts.insertElementAt(bodypart, i);
    }
  }

  /**
   * Returns a sub-message with a specified name. The name is considered to be the "name" attribute
   * of the content-disposition header of this subtype.
   *
   * @param      s    The name of the sub-message that will be returned
   * @return     A sub-message with the specified name or null if such does not exist
   * @throws     IOException   If some error occurs in reading / writing to the input stream of the message body
   * @throws     MultipartParseException   If the message body doesn't represent a valid multipart message
   */
  public MultipartPart getBodyPart(String s) throws IOException, MultipartParseException {
    synchronized (this) {
      ensureParsed();
      int i = getCount();
      for (int j = 0; j < i; j++) {
        MultipartPart mimebodypart = getBodyPart(j);
        String s1 = mimebodypart.getName();
        if (s1 != null && s1.equals(s)) {
          return mimebodypart;
        }
      }
      return null;
    }
  }

  /**
   * Adds the data sent with "multipart/form-data" type of request in the parameters set of the request.
   * Then the multipart form parameters can be retrieved using the getParameter methods of the ServletRequest object.
   *
   */
  public abstract void addFormParametersToRequest();

  /**
   * Indicates whether the data sent with "multipart/form-data" type of request is available
   * as request parameters.
   * @return true if data is available as request parameters.
   */
  public boolean isFormParametersToRequest() {
    return addFormParameters;
  }

  /**
   * Writes this multipart message to the specified output stream according to the multipart messages syntax.
   *
   * @param      outputstream    The output stream where the message will be written to
   * @throws     IOException   If some error occurs in reading / writing to the input stream of the message body
   */
  public abstract void writeTo(OutputStream outputstream) throws IOException;

  /**
   * Parses the multipart massage in smaller sub-messages.
   * @throws IOException If some error occurs in reading / writing to the input stream of the message body
   * @throws MultipartParseException If the message body doesn't represent a valid multipart message
   */
  protected abstract void parse() throws IOException, MultipartParseException;

  private void ensureParsed() throws IOException, MultipartParseException {
    if (!parsed) {
      parsed = true;
      parse();
    }
  }

  /**
   * Sets encoding for part's headers parsing.
   * If the paramether is null - no encoding will be used for string conversion.
   * @param charset The encoding to be used.
   * @throws UnsupportedEncodingException If the given encoding is not supported.
   */
  public void setCharset(String charset) throws UnsupportedEncodingException {
    if (parsed && traceLocation.beWarning()) {
    	LogContext.getLocation(LogContext.LOCATION_MULTIPART).traceWarning("ASJ.web.000576",
    			"Method setCharset(\"{0}\") is called after parsing of the request and has no effect.", new Object[] {charset}, null, null);
    }
    if (charset != null) {
      // Tests whether encoding is valid
      byte buffer[] = {(byte) 'a'};
      new String(buffer, charset);
    }
    this.charset = charset;
  }
}
