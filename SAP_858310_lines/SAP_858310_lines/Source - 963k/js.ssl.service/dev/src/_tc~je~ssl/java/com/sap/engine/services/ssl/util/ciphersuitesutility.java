/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * url: http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 *
 */
package com.sap.engine.services.ssl.util;

import com.sap.engine.services.ssl.exception.SSLResourceAccessor;
import com.sap.tc.logging.Severity;
import iaik.security.ssl.CipherSuite;
import iaik.security.ssl.CipherSuiteList;

/**
 *  Common utilities used by SSL service.
 *
 * @author  Stephan Zlatarev
 * @version 4.0.2
 */
public class CipherSuitesUtility {

  // array of cipher suite instances
  private static CipherSuite[] cipherSuites;


  public static void stop() {
    cipherSuites = null;
  }



  private static synchronized void init() {
    try {
      cipherSuites = (new CipherSuiteList(CipherSuiteList.L_ALL)).toArray();
    } catch (Exception e) {
      SSLResourceAccessor.traceThrowable(Severity.WARNING, "Failed initialization of CipherSuitesUtility", null, e);
      cipherSuites = new CipherSuite[0];
    }
  }

  /**
   *  Converts an array of cipher suites names to a list of CipherSuite instances.
   *
   * @param  suites  an array of printable names of suites.
   *
   * @return  a list of cipher suites.
   */
  public static CipherSuiteList convertToList(String[] suites) {
    CipherSuiteList ciphersuitelist;

    if ((suites == null) || (suites.length == 0)) {
      return new CipherSuiteList();
    }

    init();
    ciphersuitelist = new CipherSuiteList();

    for (int i = 0; i < suites.length; i++) {
      for (int j = 0; j < cipherSuites.length; j++) {
        if (cipherSuites[j].getName().equals(suites[i])) {
          ciphersuitelist.add(cipherSuites[j]);
        }
      }
    }

    return ciphersuitelist;
  }

  /**
   *  Converts a list of cipher suites to an array of their names.
   *
   * @param  list  a list of cipher suites.
   *
   * @return  an array of printable names of the given suites.
   */
  public static String[] convertToStringArray(CipherSuiteList list) {
    return convertToStringArray(list.toArray());
  }

  /**
   *  Converts a list of cipher suites to an array of their names.
   *
   * @param  suites  a list of cipher suites.
   *
   * @return  an array of printable names of the given suites.
   */
  public static String[] convertToStringArray(CipherSuite[] suites) {
    String[] s = new String[suites.length];

    for (int i = 0; i < s.length; i++) {
      s[i] = suites[i].toString();
    }

    return s;
  }

  /**
   *  Returns all known cipher suites.
   *
   * @return  all known cipher suites.
   */
  public static CipherSuite[] getAllCipherSuites() {
    init();
    return cipherSuites;
  }

}

