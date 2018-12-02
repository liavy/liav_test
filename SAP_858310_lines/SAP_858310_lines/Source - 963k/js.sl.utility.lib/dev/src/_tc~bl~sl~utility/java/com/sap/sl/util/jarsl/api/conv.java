package com.sap.sl.util.jarsl.api;

import java.io.File;

/**
 * Title: Conv
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company: SAP AG
 * @author  Ralf Belger
 * @version 1.0
 */

public final class Conv {
  /**
   * This static method replaces every file separator (UNIX and NT)
   * character with the system specific file separator and removes leading "./" characters.
   */
  public static String pathConv(String path) {
    String result;
    if (path==null) {
      return null;
    }
    else {
      if (File.separator.compareTo("/")==0) {
        result=path.replace('\\','/');
        if (result.startsWith("./")) {
          result=result.substring(2);
        }
        return result;
      }
      else {
        result=path.replace('/','\\');
        if (result.startsWith(".\\")) {
          result=result.substring(2);
        }
        return result;
      }
    }
  }
  /**
   * This static method replaces every NT file separator with an
   * UNIX file separator and removes leading "./" characters.
   */
  public static String pathConvJar(String path) {
    String result;
    if (path==null) {
      return null;
    }
    else {
      result=path.replace('\\','/');
      if (result.startsWith("./")) {
        result=result.substring(2);
      }
      return result;
    }
  }
}