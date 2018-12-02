/*
 * Copyright (c) 2003 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.services.webservices.server.ant;

import javax.xml.namespace.QName;

import com.sap.engine.services.webservices.webservices630.server.deploy.external.ExternalWSDeployGenerator;

/**
 * @author Alexander Zubev (alexander.zubev@sap.com)
 */
public class GenerateWSDL {
  
  public static void main(String[] args) throws Exception {
    String jar = null;
    String dir = null;
    String style = null;
    String hostAddress = "";
    boolean sapMode = false;
    String wsURI = null;
    String wsLocalName = null;
    
    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      if (arg.startsWith("jar=")) {
        jar = arg.substring(4);        
      } else if (arg.startsWith("dir=")) {
        dir = arg.substring(4);
      } else if (arg.equals("sapMode")) {
        sapMode = true;
      } else if (arg.startsWith("style=")) {
        style = arg.substring(6);
        if (style.length() == 0) {
          style = null;
        }
      } else if (arg.startsWith("hostAddress=")) {
        hostAddress = arg.substring(12);
      } else if (arg.startsWith("wsURI=")) {
        wsURI = arg.substring(6);
      } else if (arg.startsWith("wsLocalName=")) {
        wsLocalName = arg.substring(12);
      }
    }
    
    ExternalWSDeployGenerator generator = new ExternalWSDeployGenerator(dir);
    QName webservice = null;
    if (wsURI != null && wsLocalName != null) {
      webservice = new QName(wsURI, wsLocalName);
    }
    generator.generateStandAloneWsdls(jar, dir, style, hostAddress, webservice, sapMode);
  }
}
