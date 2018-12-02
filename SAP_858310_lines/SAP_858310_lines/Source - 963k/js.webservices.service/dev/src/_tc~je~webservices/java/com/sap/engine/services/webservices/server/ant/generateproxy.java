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

import java.io.File;

import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProxyGenerator;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProxyGeneratorConfig;

/**
 * @author Alexander Zubev (alexander.zubev@sap.com)
 */
public class GenerateProxy {
  public static synchronized File getTempDir() {
    String tempDirectory = System.getProperty("java.io.tmpdir", ".");
    File tempDir;
    synchronized (tempDirectory) {
      do {
        tempDir = new File(tempDirectory, "proxy" + System.currentTimeMillis());
      } while (tempDir.exists());
      tempDir.mkdirs();
    }
    return tempDir;
  }

  public static void removeDirectory(File tempDir) {
    if (tempDir == null || !tempDir.exists()) {
      return;
    }
    File[] files = tempDir.listFiles();
  
    if (files != null) {
      for (int i = 0; i < files.length; i++) {
        if (files[i].isDirectory()) {
          removeDirectory(files[i]);
        }
  
        files[i].delete();
      }
    }
  
    tempDir.delete();
  }

  public static void main(String[] args) throws Exception {
    String wsdl = null;
    String packageName = null;
    String jar = null;
    boolean standalone = false;
    boolean rpcStyleMethods = false;
    boolean rpcInterfaces = false;
    
    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      if (arg.startsWith("wsdl=")) {
        wsdl = arg.substring(5);  
      } else if (arg.startsWith("packageName=")) {
        packageName = arg.substring(12);
      } else if (arg.startsWith("jar=")) {
        jar = arg.substring(4);
      } else if (arg.equals("standalone")) {
        standalone = true;
      } else if (arg.equals("rpcStyleMethods")) {
        rpcStyleMethods = true;
      } else if (arg.equals("rpcInterfaces")) {
        rpcInterfaces = true;
      }
    }

    File tempDir = getTempDir();
    try {
      String tempDirString = tempDir.getAbsolutePath();
        
      ProxyGenerator generator = new ProxyGenerator();
      ProxyGeneratorConfig config = new ProxyGeneratorConfig(wsdl, tempDirString, packageName);
      config.setJarName(jar);
      config.setCompile(true);
  
      config.setInterfacesOnly(!standalone);
      config.setAdditionalMethods(rpcStyleMethods);
      config.setJaxRpcMethods(rpcInterfaces);
      config.setJarExtensions(new String[] {"class", "java", "xml", "txt", "properties"});
      generator.generateProxy(config);
    } finally {
      removeDirectory(tempDir);
    }    
  }
}
