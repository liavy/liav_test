/*
 * Copyright (c) 2002 by SAP Labs Sofia AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.webservices.jaxrpc.wsdl2java.soapbinding;

import java.io.Serializable;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.ArrayList;

/**
 * Cookie representation.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class Cookie implements Serializable, Cloneable {

  private String name;
  private String value;
  private String comment;
  private String domain;
  private String maxAge;
  private String path;
  private String secure;
  private String version;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public String getMaxAge() {
    return maxAge;
  }

  public void setMaxAge(String maxAge) {
    this.maxAge = maxAge;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getSecure() {
    return secure;
  }

  public void setSecure(String secure) {
    this.secure = secure;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public static String getCookieAsResponseLine(Cookie cookie) {
    StringBuffer result = new StringBuffer();
    result.append(cookie.name);
    result.append("=");
    result.append(cookie.value);
    result.append(";");
    if (cookie.domain != null) {
      result.append("DOMAIN="+cookie.domain+";");
    }
    if (cookie.path != null) {
      result.append("PATH="+cookie.path+";");
    }
    if (cookie.version != null) {
      result.append("VERSION="+cookie.version+";");
    }    
    if (result.charAt(result.length()-1)==';') {
      result.setLength(result.length()-1);
    }
    return result.toString();
  }
  
  /**
   * Parses String of cookies.
   * @param cookieLine
   * @return
   */
  public static ArrayList readCookies(String cookieLine) {
    ArrayList result = new ArrayList();
    StringTokenizer tokenizer = new StringTokenizer(cookieLine,";",false);
    Cookie currentCookie = new Cookie();
    while (tokenizer.hasMoreElements()) {
      String token = tokenizer.nextToken().trim();
      if ("Secure".equals(token)) { // secure option.
        continue; // do nothing
      }
      if (token.startsWith("Max-Age=")) {
        continue; // do nothing
      }
      if (token.startsWith("Comment=")) {
        continue; // do nothing
      }
      if (token.toUpperCase(Locale.ENGLISH).startsWith("DOMAIN=")) {
        currentCookie.setDomain(token.substring("Domain=".length()).trim());
        continue;
      }
      if (token.toUpperCase(Locale.ENGLISH).startsWith("PATH=")) {
        currentCookie.setPath(token.substring("Path=".length()).trim());
        continue;
      }
      if (token.toUpperCase(Locale.ENGLISH).startsWith("VERSION=")) {
        currentCookie.setVersion(token.substring("Version=".length()).trim());
        continue;
      }
      if ("Discard".equals(token)) {
        // do nothing
        continue;
      }
      if (token.length() != 0) { // this is a cookie
        if (currentCookie.getName() != null) {
          result.add(currentCookie);
          currentCookie = new Cookie();
        }
        int index = token.indexOf('=');
        if (index != -1) {
          currentCookie.setName(token.substring(0,index));
          currentCookie.setValue(token.substring(index+1));
        }
      }
    }
    if (currentCookie.getName() != null) {
      result.add(currentCookie);
    }
    return result;
  }

  public static boolean updateCookies(Cookie newCookie, ArrayList cookies) {
    for (int i=0; i<cookies.size(); i++) {
      Cookie cookieLine = (Cookie) cookies.get(i);
      if (cookieLine.getName().equals(newCookie.getName())) {
        cookieLine.setValue(newCookie.getValue());
        cookieLine.setPath(newCookie.getPath());
        cookieLine.setDomain(newCookie.getDomain());
        return true;
      }
    }
    return false;
  }

  public static void updateCookies(ArrayList newCookies, ArrayList cookies) {
    ArrayList perm = new ArrayList();
    for (int i=0; i<newCookies.size(); i++) {
      if (updateCookies((Cookie) newCookies.get(i),cookies) == false) {
        perm.add(newCookies.get(i));
      }
    }
    for (int i=0; i<perm.size();i++) {
      cookies.add(perm.get(i));
    }
  }

  /**
   * Returns a cookie string.
   * @param cookies
   * @return
   */
  public static String getAsRequestString(Cookie[] cookies) {
    String version = null;
    StringBuffer result = new StringBuffer();
    for (int i=0; i<cookies.length; i++) {
      result.append(cookies[i].getName());
      result.append('=');
      result.append(cookies[i].getValue());
      if (cookies[i].getPath() != null) {
        result.append(";$Path=");
        result.append(cookies[i].getPath());
      }
      if (cookies[i].getDomain() != null) {
        result.append(";$Domain=");
        result.append(cookies[i].getDomain());
      }
      if ((cookies.length-1) != i) {
        result.append(';');
      }
      if (cookies[i].getVersion() != null) {
        version = "$Version="+cookies[i].getVersion();
      }
    }
    return version+result.toString();
  }

  /**
   * Returns a cookie string.
   * @param cookie
   * @return
   */
  public static String getAsRequestString(Cookie cookie) {
    StringBuffer result = new StringBuffer();
    if (cookie.getVersion() != null) {
      result.append("$Version=");
      result.append(cookie.getVersion());
      result.append(';');
    }
    result.append(cookie.getName());
    result.append('=');
    result.append(cookie.getValue());
    if (cookie.getPath() != null) {
      result.append(";$Path=");
      result.append(cookie.getPath());
    }
      if (cookie.getDomain() != null) {
        result.append(";$Domain=");
        result.append(cookie.getDomain());
      }
    return result.toString();
  }

  public Object clone() {
    try {
      Cookie result = (Cookie) super.clone();
      result.name = this.name;
      result.value = this.value;
      result.comment = this.comment;
      result.domain = this.domain;
      result.maxAge = this.maxAge;
      result.path = this.path;
      result.secure = this.secure;
      result.version = this.version;
      return result; 
    } catch (CloneNotSupportedException cnse) {
      // this shouldn't happen, since we are cloneable
      throw new InternalError();
    }         
  }

}
