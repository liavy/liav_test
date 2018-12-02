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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;

/**
 * @author Alexander Zubev
 */
public class DynamicStubsLoader extends ClassLoader {
  private Hashtable generatedStubs;

  public DynamicStubsLoader(ClassLoader parent, File generatedDir) throws IOException {
    super(parent);
    generatedStubs = new Hashtable();
    addClassFiles(null, generatedDir);
  }

  private void addClassFiles(String packageName, File dir) throws IOException {
    File[] files = dir.listFiles();
    for (int i = 0; i < files.length; i++) {
      File file = files[i];
      if (file.isDirectory()) {
        String newPackage;
        if (packageName == null) {
          newPackage = file.getName();
        } else {
          newPackage = packageName + "." + file.getName();
        }
        addClassFiles(newPackage, file);
      } else {
        String className;
        if (packageName == null) {
          className = file.getName();
        } else {
          className = packageName + "." + file.getName();
        }
        int index = className.lastIndexOf(".");
        if (index != -1) {
          String extension = className.substring(index + 1);
          if (!"class".equals(extension)) {
            continue;
          }
          className = className.substring(0, index);

          FileInputStream in = new FileInputStream(file);
          int len = in.available();
          byte[] result = new byte[len];
          int n = 0;

          while (n < len) {
            int count = in.read(result, n, len - n);
            if (count < 0) {
              break;
            }
            n += count;
          }
          in.close();

          defineClass(className, result, 0, n);
        }
      }
    }
  }

  public Class loadClass(String name) throws ClassNotFoundException {
    try {
      return super.loadClass(name);
    } catch (Throwable thr) {
      return getParent().loadClass(name);
    }
  }
}
