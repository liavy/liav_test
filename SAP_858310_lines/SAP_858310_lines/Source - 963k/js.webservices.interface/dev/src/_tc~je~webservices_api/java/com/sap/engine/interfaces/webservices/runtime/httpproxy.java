/*
 * Copyright (c) 2003 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.interfaces.webservices.runtime;


import java.io.Serializable;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * @author Alexander Zubev (alexander.zubev@sap.com)
 */
public class HTTPProxy implements Serializable {
  private String proxyHost;
  private int proxyPort = 80;
  private String proxyUser;
  private String proxyPass;
  private String excludeList;
  private boolean bypassLocalAddresses = true;

  /**
   * @return
   */
  public String getExcludeList() {
    return excludeList;
  }

  /**
   * @return
   */
  public String getProxyHost() {
    return proxyHost;
  }

  /**
   * @return
   */
  public int getProxyPort() {
    return proxyPort;
  }

  /**
   * @param excludeList
   */
  public void setExcludeList(String excludeList) {
    this.excludeList = excludeList.toLowerCase(Locale.ENGLISH);
  }

  /**
   * @param proxyHost
   */
  public void setProxyHost(String proxyHost) {
    this.proxyHost = proxyHost;
  }

  /**
   * @param proxyPort
   */
  public void setProxyPort(int proxyPort) {
    this.proxyPort = proxyPort;
  }

  public boolean useProxyForAddress(String address) {
  	if (proxyHost == null || "".equals(proxyHost)) {
  		return false;
  	}
    address = address.toLowerCase(Locale.ENGLISH);
    if (bypassLocalAddresses) {
      if (address.indexOf(".") == -1 || "127.0.0.1".equals(address)) {
        return false;
      }
    }
    return useProxyForSpecificAddress(address, excludeList);
  }

  private static boolean useProxyForSpecificAddress(String address, String excludeList) {
    //    if ("localhost".equals(address) || "127.0.0.1".equals(address)) {
    //      return false;
    //    }
    if (excludeList == null || excludeList.trim().length() == 0) {
      return true;
    }
    StringTokenizer tokenizer = new StringTokenizer(excludeList, ";,| ", false);
    while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken().trim();
      if (match(token, 0, address, 0)) {
        return false;
      }
//      int starIndex = token.indexOf("*");
//      if (starIndex != -1) {
//        String start = token.substring(0, starIndex);
//        String end = token.substring(starIndex + 1);
//        if (address.startsWith(start) && address.endsWith(end)) {
//          return false;
//        }
//      } else if (token.equals(address)) {
//        return false;
//      }
    }
    return true;
  }

  public static boolean match(String pattern, int patternStart, String string, int stringStart) {
  	pattern = pattern.toUpperCase(Locale.ENGLISH);
  	string = string.toUpperCase(Locale.ENGLISH);
    for (int s = stringStart, p = patternStart; ; ++p, ++s) {
      boolean sEnd = (s >= string.length());
      boolean pEnd = (p >= pattern.length());
      if (sEnd && pEnd) {
        return true;
      }
      if (pEnd) {
        return false;
      }
      if (sEnd) {
        if (pattern.charAt(p) == '*') {
          s--;//we can catch a pattern ending with ****
          continue;
        }

        return false;
      }
      if (pattern.charAt(p) == '?') {
        continue;
      }
      if (pattern.charAt(p) == '*') {
        ++p;
        pEnd = (p >= pattern.length());
        if (pEnd) {
          return true;
        }
        for (int i = s; i < string.length(); ++i) {
          if (match(pattern, p, string, i)) {
            return true;
          }
        }
        return false;
      }
      if (pattern.charAt(p) != string.charAt(s)) {
        return false;
      }
    }
  }

  /**
   * @return
   */
  public boolean isBypassLocalAddresses() {
    return bypassLocalAddresses;
  }

  /**
   * @param b
   */
  public void setBypassLocalAddresses(boolean b) {
    bypassLocalAddresses = b;
  }

  /**
   * @return
   */
  public String getProxyPass() {
    return proxyPass;
  }

  /**
   * @return
   */
  public String getProxyUser() {
    return proxyUser;
  }

  /**
   * @param proxyPass
   */
  public void setProxyPass(String proxyPass) {
    this.proxyPass = proxyPass;
  }

  /**
   * @param proxyUser
   */
  public void setProxyUser(String proxyUser) {
    this.proxyUser = proxyUser;
  }
}