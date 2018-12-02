package com.sap.engine.services.webservices.runtime.wsdl;

import java.util.Hashtable;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */

public class PrefixFactory {

  private static String BASE_PREFIX  =  "ns";

  private int number;
  private Hashtable mappings;   //key: uri, value: prefix

  public PrefixFactory() {
    mappings = new Hashtable();
    number = 0;
  }

  /**
   * Only for internal use ! ! !
   */
  public void registerPrefix(String prefix, String uri) {
    mappings.put(uri, prefix);
  }

  public synchronized String getPrefix(String uri) {
    String prefix = (String) mappings.get(uri);
    if (prefix != null) {
      return prefix;
    }

    prefix = BASE_PREFIX + (number++);
    mappings.put(uri, prefix);

    return prefix;
  }

  public void clear() {
    this.mappings.clear();;
    number = 0;
  }

  public Hashtable getMappings() {
    return this.mappings;
  }

}