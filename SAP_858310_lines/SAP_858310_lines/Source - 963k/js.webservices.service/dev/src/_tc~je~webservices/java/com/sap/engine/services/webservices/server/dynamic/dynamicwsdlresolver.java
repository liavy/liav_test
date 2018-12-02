/*
 * Copyright (c) 2003 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.services.webservices.server.dynamic;

import java.io.InputStream;
import java.io.IOException;
import java.io.File;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * @author Alexander Zubev
 */
public class DynamicWSDLResolver implements EntityResolver {
  private Class wsInterface;

  public DynamicWSDLResolver(Class wsInterface) {
    this.wsInterface = wsInterface;
  }

  public InputSource getFromClassLoader(String resourcePath) throws IOException {
    InputStream in = wsInterface.getResourceAsStream(resourcePath);
    if (in == null) {
      in = wsInterface.getClassLoader().getResourceAsStream(resourcePath);
      if (in == null) {
        throw new IOException("Cannot load from classloader: " + resourcePath);
      }
    }
    InputSource source = new InputSource(in);
    source.setSystemId(resourcePath);
    return source;
  }

  public InputSource resolveEntity(String publicID, String systemID) throws IOException {
    if (systemID.lastIndexOf(":") != -1) {
      systemID = systemID.substring(systemID.lastIndexOf(":") + 1);
    }
    if (systemID.lastIndexOf("\\") != -1) {
      systemID = systemID.substring(systemID.lastIndexOf("\\") + 1);
    }
    if (systemID.lastIndexOf("/") != -1) {
      systemID = systemID.substring(systemID.lastIndexOf("/") + 1);
    }
    String resourcePath = "/META-INF/ws-clients-descriptors/" + systemID;
    return getFromClassLoader(resourcePath);
  }
}
