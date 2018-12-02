/*
 * Copyright (c) 2005 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.jaxws;

import java.util.HashSet;
import java.util.Locale;

import com.sun.tools.xjc.api.ClassNameAllocator;

public class ClassAllocator implements ClassNameAllocator {
  
  //public String outputPackageName = "";
  public HashSet<String> outputInterfaceNames = new HashSet<String>();

  public String assignClassName(String arg0, String arg1) {
    String qualifiedName = arg1;
    if (arg0 != null && arg0.length() > 0) {
      qualifiedName = arg0+"."+arg1;  
    }
    if (outputInterfaceNames.contains(qualifiedName.toLowerCase(Locale.ENGLISH))) {
      return arg1+"_Type";
    }
    return arg1;
  }

}
