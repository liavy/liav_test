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
package com.sap.engine.services.servlets_jsp.server.lib;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import com.sap.engine.lib.security.http.HttpSecureSession;
import com.sap.engine.lib.util.HashMapObjectObject;
import com.sap.engine.services.httpserver.interfaces.HttpParameters;
import com.sap.engine.services.httpserver.interfaces.client.Request;
import com.sap.engine.services.httpserver.lib.CookieParser;
import com.sap.engine.services.httpserver.lib.protocol.HeaderNames;
import com.sap.engine.services.httpserver.lib.protocol.Methods;
import com.sap.engine.services.httpserver.lib.util.ByteArrayUtils;
import com.sap.engine.services.httpserver.lib.util.MessageBytes;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.runtime.client.ApplicationSession;

public class WebParseUtils {
  private static final byte[] application_x_www_form_urlencoded = "application/x-www-form-urlencoded".getBytes();

  public static String parseEncoding(HttpParameters httpParameters) {
    byte[] contentType = httpParameters.getRequest().getHeaders().getHeader(HeaderNames.entity_header_content_type_);
    if (contentType != null) {

      StringTokenizer tokenizer = null;
      try{
        tokenizer = new StringTokenizer(new String(contentType,"UTF-8"),";");
      }catch(UnsupportedEncodingException ex){
        tokenizer = new StringTokenizer(new String(contentType),";");
      }
      while(tokenizer.hasMoreElements()){
          byte[] token = tokenizer.nextToken().trim().getBytes();
          int charsetLocation = ByteArrayUtils.indexOf(token, Constants.charset);
          if (charsetLocation == 0){
            String charset = StringUtils.unquote(token, charsetLocation + 8, token.length - (charsetLocation + 8));
            if (LogContext.getLocationServletRequest().bePath()){
                tracePath("charset","Content-Type charset: ["+charset+"]",httpParameters.getRequest().getClientId());
            }
            return charset;
          }
      }
    }
    return null;
  }

  /**
   * Parses the query string 'data' and accumulates the new parameters in 'parameters'.
   * @param parameters
   * @param data
   * @param characterEncoding
   * @throws UnsupportedEncodingException
   */
  public static void parseQueryString(HashMapObjectObject parameters, byte[] data, String characterEncoding) throws UnsupportedEncodingException {
    if (data == null || data.length == 0) {
      return;
    }
    int ix = 0;
    int ox = 0;
    HashMapObjectObject newParameters = new HashMapObjectObject(8);
    String key = null;
    while (ix < data.length) {
      byte c = data[ix++];
      switch (c) {
        case '&': {
          if (key != null) {
            putMapEntry(newParameters, key, new String(data, 0, ox, characterEncoding));
            key = null;
          } else {  // when has only parameter name
            if( ox > 0) { //otherwise we have && in the request line - http://localhost:50000/kol.jsp?a=1&&b=2
              putMapEntry(newParameters, new String(data, 0, ox, characterEncoding),"");
            }
          }
          ox = 0;
          break;
        }
        case '=': {
          if (key == null) {
            key = new String(data, 0, ox, characterEncoding);
            ox = 0;
          } else {
            data[ox++] = c;
          }
          break;
        }
        case '+': {
          data[ox++] = (byte) ' ';
          break;
        }
        case '%': {
          if (ix < data.length - 1) {
            data[ox++] = (byte) ((convertHexDigit(data[ix++]) << 4) + convertHexDigit(data[ix++]));
          } else {
            data[ox++] = c;
          }
          break;
        }
        default: {
          data[ox++] = c;
        }
      }
    }
    if (key != null) {
      putMapEntry(newParameters, key, new String(data, 0, ox, characterEncoding));
    } else if (ox > 0){  // when has only parameter name
      putMapEntry(newParameters, new String(data, 0, ox, characterEncoding),"");
    }
    mergeParameters(parameters, newParameters);
  }

  public static void parseParameters(HashMapObjectObject parameters, HttpParameters httpParameters, String characterEncoding) throws UnsupportedEncodingException {
    boolean bePath = LogContext.getLocationServletRequest().bePath();
  	if (bePath) {
      tracePath("parseParameters", "request method: [" + new String(httpParameters.getRequest().getRequestLine().getMethod())
          + "]", httpParameters.getRequest().getClientId());
    }
    if (ByteArrayUtils.equalsBytes(httpParameters.getRequest().getRequestLine().getMethod(), Methods._POST)) {
      byte[] contentType = httpParameters.getRequest().getHeaders().getHeader(HeaderNames.entity_header_content_type_);
      if (bePath) {
        if (contentType == null) {
          tracePath("parseParameters", "request content type: [" + contentType
              + "]", httpParameters.getRequest().getClientId());
        } else {
          tracePath("parseParameters", "request content type: [" + new String(contentType)
              + "]", httpParameters.getRequest().getClientId());
        }
      }
      int ind = ByteArrayUtils.indexOf(contentType, application_x_www_form_urlencoded);
      if (ind == 0 || ind == 1) {
        if (contentType.length <= ind + application_x_www_form_urlencoded.length
            || contentType[application_x_www_form_urlencoded.length] == ' '
            || contentType[application_x_www_form_urlencoded.length] == '\t'
            || contentType[application_x_www_form_urlencoded.length] == ';') {
          if (bePath) {
            tracePath("parseParameters", "request content length: ["
                + httpParameters.getRequest().getHeaders().getIntHeader(HeaderNames.entity_header_content_length_)
                + "]", httpParameters.getRequest().getClientId());
          }
          if (httpParameters.getRequestParametersBody() != null) {
            parseQueryString(parameters, httpParameters.getRequestParametersBody().getBytes(), characterEncoding);
          }
        }
      }
    }

    // TODO: Check if this method is called and remove it if yes
    // If there is 'j_auth_passed' security attribute this means that
    // the request should be the original one after successful form login
    // and 'j_request_parameters' attribute holds the saved parameters
//    ApplicationSession session =
//      (ApplicationSession) httpParameters.getApplicationSession();
//    if (session != null) {
//	    Object value = session.removeSecurityAttribute(
//	      HttpSecureSession.SECURITY_VALUE_J_AUTH_PASSED);
//	    if (value != null && value instanceof String) {
//	      if (((String) value).equals(getRequestURL(httpParameters.getRequest()))) {
//	        value = session.removeSecurityAttribute(
//	          HttpSecureSession.SECURITY_VALUE_J_REQUEST_PARAMETERS);
//	        if (value != null && value instanceof Map) {
//	          HashMapObjectObject savedParameters = new HashMapObjectObject();
//	          convert((Map) value, savedParameters);
//	          mergeParameters(parameters, savedParameters);
//	        }
//	      }
//	    }
//    }

    if (httpParameters.getRequest().getRequestLine().getQuery() != null) {
      parseQueryString(parameters, httpParameters.getRequest().getRequestLine().getQuery().getBytes(), characterEncoding);
    }
  }

  /**
   * 
   * @param parameters
   * @param httpParameters
   * @param query
   * @param characterEncoding
   * @param parseBody - this parameter specifies if parse post data  - it is
   * connected with post parameters preservation scenarios. It is introduced because the post data of
   * the original request should be restored
   * @throws UnsupportedEncodingException
   */
  public static void parseParameters(HashMapObjectObject parameters, HttpParameters httpParameters,
                                     byte[] query, String characterEncoding, boolean parseBody) throws UnsupportedEncodingException {
    boolean bePath = LogContext.getLocationServletRequest().bePath();
  	if (bePath) {
      tracePath("parseParameters", "request method: [" + new String(httpParameters.getRequest().getRequestLine().getMethod())
          + "]", httpParameters.getRequest().getClientId());
    }
    if (ByteArrayUtils.equalsBytes(httpParameters.getRequest().getRequestLine().getMethod(), Methods._POST) && parseBody) {
      byte[] contentType = httpParameters.getRequest().getHeaders().getHeader(HeaderNames.entity_header_content_type_);
      if (bePath) {
        if (contentType == null) {
          tracePath("parseParameters", "request content type: [" + contentType
              + "]", httpParameters.getRequest().getClientId());
        } else {
          tracePath("parseParameters", "request content type: [" + new String(contentType)
              + "]", httpParameters.getRequest().getClientId());
        }
      }
      int ind = ByteArrayUtils.indexOf(contentType, application_x_www_form_urlencoded);
      if (ind == 0 || ind == 1) {
        if (contentType.length <= ind + application_x_www_form_urlencoded.length
            || contentType[application_x_www_form_urlencoded.length] == ' '
            || contentType[application_x_www_form_urlencoded.length] == '\t'
            || contentType[application_x_www_form_urlencoded.length] == ';') {
          if (bePath) {
            tracePath("parseParameters", "request content length: ["
                + httpParameters.getRequest().getHeaders().getIntHeader(HeaderNames.entity_header_content_length_)
                + "]", httpParameters.getRequest().getClientId());
          }
          if (httpParameters.getRequestParametersBody() != null) {
            parseQueryString(parameters, httpParameters.getRequestParametersBody().getBytes(), characterEncoding);
          }
        }
      }
    }

    // If there is 'j_auth_passed' security attribute this means that
    // the request should be the original one after successful form login
    // and 'j_request_parameters' attribute holds the saved parameters
//    ApplicationSession session =
//      (ApplicationSession) httpParameters.getApplicationSession();
//    if (session != null) {
//	    Object value = session.removeSecurityAttribute(
//	      HttpSecureSession.SECURITY_VALUE_J_AUTH_PASSED);
//	    if (value != null && value instanceof String) {
//	      if (((String) value).equals(getRequestURL(httpParameters.getRequest()))) {
//	        value = session.removeSecurityAttribute(
//	          HttpSecureSession.SECURITY_VALUE_J_REQUEST_PARAMETERS);
//	        if (value != null && value instanceof Map) {
//	          HashMapObjectObject savedParameters = new HashMapObjectObject();
//	          convert((Map) value, savedParameters);
//	          mergeParameters(parameters, savedParameters);
//	        }
//	      }
//	    }
//    }

    if (query != null && query.length > 0) {
      parseQueryString(parameters, query, characterEncoding);
    }
  }

  /**
   * Encodes some URL as a valid URL request containing session information.
   *
   * @param   url  some URL
   * @param   sessionId  session id that will be added to the UPL
   * @return     the encoded URL with session id
   */
  public static String encodeURL(String url, String sessionId, boolean setSessionCookie,
  		String applicationCookieEncoded, String appCookieName, int clientId) {
    if (url == null) {
      return null;
    }

    String query = null;
    String encodedData = null;
    int sep = url.indexOf('?');
    if (sep > -1) {
      query = url.substring(sep);
      url = url.substring(0, sep);
    }
    sep = url.indexOf(';');
    if (sep > -1) {
      encodedData = url.substring(sep);
      url = url.substring(0, sep);
    }

    boolean disableURLSessionTracking = ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().isURLSessionTrackingDisabled();
    if (LogContext.getLocationServletRequest().bePath()) {
    	tracePath("encodeURL", "disableURLSessionTracking = [" + disableURLSessionTracking + "], url = [" + url +
      		"], sessionId = [" + sessionId + "], setSessionCookie = [" + setSessionCookie + "]", clientId);
    }

    if (!disableURLSessionTracking) {
	    if (setSessionCookie) {
	      url += CookieParser.jsessionid_url_sep + sessionId;
	    }
	    url += applicationCookieEncoded;
    }

    if (encodedData != null && (sep = encodedData.indexOf(CookieParser.jsessionid_url_sep)) > -1) {
      if (sep == 0) {
        sep = encodedData.indexOf(';', sep + 1);
        if (sep > -1) {
          encodedData = encodedData.substring(sep);
        } else {
          encodedData = null;
        }
      } else {
        String encodedDataBeg = encodedData.substring(0, sep - 1);
        sep = encodedData.indexOf(';', sep + 1);
        if (sep > -1) {
          encodedData = encodedDataBeg + encodedData.substring(sep);
        } else {
          encodedData = encodedDataBeg;
        }
      }
    }

    if (encodedData != null
        && (sep = encodedData.indexOf(";" + appCookieName + "=")) > -1) {
      if (sep == 0) {
        sep = encodedData.indexOf(';', sep + 1);
        if (sep > -1) {
          encodedData = encodedData.substring(sep);
        } else {
          encodedData = null;
        }
      } else {
        String encodedDataBeg = encodedData.substring(0, sep);
        sep = encodedData.indexOf(';', sep + 1);
        if (sep > -1) {
          encodedData = encodedDataBeg + encodedData.substring(sep);
        } else {
          encodedData = encodedDataBeg;
        }
      }
    }

    if (encodedData != null) {
      url += encodedData;
    }
    if (query != null) {
      url += query;
    }
    return url;
  }

  // ---------- > PRIVATE < ----------

  private static void putMapEntry(HashMapObjectObject parameters, String name, String value) {
    String[] oldValues = (String[])parameters.get(name);
    if (oldValues == null) {
      oldValues = new String[1];
    } else {
      String[] tmp = oldValues;
      oldValues = new String[tmp.length + 1];
      System.arraycopy(tmp, 0, oldValues, 0, tmp.length);
    }
    oldValues[oldValues.length - 1] = value;
    parameters.put(name, oldValues);
  }

  private static void mergeParameters(HashMapObjectObject dest, HashMapObjectObject src) {
    if (src == null) {
      return;
    }
    if (dest == null) {
      dest = src;
      return;
    }
    Object[] key = src.getAllKeys();
    String[] srcValue, destValue, temp;
    for (int i = 0; i < key.length; i++) {
      srcValue = (String[])src.get(key[i]);
      destValue = (String[])dest.get(key[i]);
      if (destValue == null) {
        dest.put(key[i], srcValue);
      } else {
        temp = new String[srcValue.length + destValue.length];
        System.arraycopy(srcValue, 0, temp, 0, srcValue.length);
        System.arraycopy(destValue, 0, temp, srcValue.length, destValue.length);
        dest.put(key[i], temp);
      }
    }
  }

  private static byte convertHexDigit(byte b) {
    if (b >= '0' && b <= '9') {
      return (byte) (b - '0');
    }
    if (b >= 'a' && b <= 'f') {
      return (byte) (b - 'a' + 10);
    }
    if (b >= 'A' && b <= 'F') {
      return (byte) (b - 'A' + 10);
    }
    return 0;
  }

  private static void tracePath(String method, String msg, int clientId) {
    LogContext.getLocationServletRequest().pathT(
    		"client [" + clientId + "] WebParseUtils." + method + " " + msg);
  }

  /**
   * Converts content of a java.util.Map to a
   * com.sap.engine.lib.util.HashMapObjectObject
   * <p>
   * Method doesn't check if the incoming arguments are <code>null</code>
   *
   * @param from
   * @param to
   */
  private static void convert(Map from, HashMapObjectObject to) {
    Iterator iter = from.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry param = (Map.Entry) iter.next();
      to.put(param.getKey(), param.getValue());
    }
  }

  /**
   * This method have to return the same as
   * <code>javax.servlet.http.HttpServletRequest.getRequestURL()</code>
   * <p>
   * Method doesn't check if the incoming argument is <code>null</code>
   *
   * @param request
   * @return reconstructed URL
   */
  private static String getRequestURL(Request request) {
    StringBuffer buff = new StringBuffer();
    buff.append(request.getScheme());
    buff.append("://");
    buff.append(request.getHost());
    buff.append(":");
    buff.append(request.getPort());
    buff.append(request.getRequestLine().getUrlDecoded());
    MessageBytes query = request.getRequestLine().getQuery();
    if (query != null) {
      buff.append("?");
      buff.append(query.toString());
    }

    return buff.toString();
  }
}
