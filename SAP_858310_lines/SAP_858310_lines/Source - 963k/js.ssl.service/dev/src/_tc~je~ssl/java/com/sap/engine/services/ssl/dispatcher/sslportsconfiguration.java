/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * url: http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.ssl.dispatcher;

import com.sap.engine.services.ssl.factory.ServerSocketFactory;

import java.util.Vector;
import java.util.Properties;

public class SSLPortsConfiguration {

  public static String[] getUsedCredentialAliases() {
    Properties properties = ((ServerSocketFactory) ServerSocketFactory.getDefault()).getSocketsProperties();
    String key = null;
    String value = null;
    Object[] keys = properties.keySet().toArray();
    Vector credentials = new Vector();
    String[] result = null;

    for (int i = 0; i < keys.length; i++) {
      key = (String) keys[i];
      value = properties.getProperty(key);

      if ((key.indexOf(".cert.") > 0) && !credentials.contains(value)) {
        credentials.add(value);
      }
    }

    result = new String[credentials.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = (String) credentials.elementAt(i);
    }

    return result;
  }

}
