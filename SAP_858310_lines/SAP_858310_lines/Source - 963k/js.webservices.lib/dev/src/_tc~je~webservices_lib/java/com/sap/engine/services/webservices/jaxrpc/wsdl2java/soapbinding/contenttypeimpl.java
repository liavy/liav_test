/*
 * Copyright (c) 2002 by SAP Labs Sofia AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.webservices.jaxrpc.wsdl2java.soapbinding;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParameterList;

/**
 * 
 *
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class ContentTypeImpl {
  static final String DELIMITER  =  ";";

  private String originalValue;
  private String baseType;
  private String subType;
  private String primaryType;
  private Hashtable parameters = new Hashtable();

  public ContentTypeImpl(String value) {
    init(value);
  }

  private void init(String value) {
    this.originalValue = value.trim();
    if (originalValue.indexOf(DELIMITER) == -1) {
      this.baseType = originalValue;
      int del = baseType.indexOf("/");
      if (del == -1) {
        throw new IllegalArgumentException("Incorrect value: '" + this.originalValue + "'");
      } else {
        this.primaryType = originalValue.substring(0, del);
        this.subType = originalValue.substring(del + 1);
        return;
      }
    }

    StringTokenizer tonenizer = new StringTokenizer(originalValue, DELIMITER);
    String next;
    int del;
    while (tonenizer.hasMoreTokens()) {
      next = tonenizer.nextToken();
      if (baseType == null) {
        init(next);
      } else {
        del = next.indexOf("=");
        if (del != -1) {
          String contentTypeParam = next.substring(del+1);
          if (contentTypeParam.startsWith("\"") && contentTypeParam.endsWith("\"")) {
            contentTypeParam = contentTypeParam.substring(1,contentTypeParam.length()-1);
          }
          parameters.put(next.substring(0, del), contentTypeParam);
        } else {
          throw new IllegalArgumentException("Incorrect value: " + this.originalValue + "'");
        }
      }
    }

  }
  public String getBaseType() {               
    return this.baseType;
  }

  public String getPrimaryType() {
    return this.primaryType;
  }

  public String getSubType() {
    return this.subType;
  }

  public String getParameter(String name) {
    return (String) parameters.get(name);
  }

  public ContentType createStandardType() {
    ParameterList list = new ParameterList();
    Enumeration en = parameters.keys();
    String next;

    while (en.hasMoreElements()) {
      next = (String) en.nextElement();
      String content = (String) parameters.get(next);
//      System.out.println("param :"+next);
//      System.out.println("value :"+content);
      list.set(next, content);
    }

    return new ContentType(getPrimaryType(), getSubType(), list);
  }
  
  public String toString() {
    return originalValue;
  }

}
