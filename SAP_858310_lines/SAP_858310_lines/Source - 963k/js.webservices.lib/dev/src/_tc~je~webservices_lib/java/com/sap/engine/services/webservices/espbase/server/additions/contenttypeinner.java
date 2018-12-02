package com.sap.engine.services.webservices.espbase.server.additions;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParameterList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */

public class ContentTypeInner {

  static final String DELIMITER  =  ";";

//  private String originalValue;
  private String baseType;
  private String subType;
  private String primaryType;
  private Hashtable parameters = new Hashtable();

  public ContentTypeInner(String value) {
    init(value);
  }

  private void init(String value) {
    if (value == null) {
      throw new IllegalArgumentException("Content-type value could not be null.");
    }
    value = value.trim();
    if (value.indexOf(DELIMITER) == -1) {
      this.baseType = value;
      int del = baseType.indexOf("/");
      if (del == -1) {
        throw new IllegalArgumentException("Incorrect value: '" + value + "'");
      } else {
        this.primaryType = value.substring(0, del);
        this.subType = value.substring(del + 1);
        return;
      }
    }

    StringTokenizer tonenizer = new StringTokenizer(value, DELIMITER);
    String next;
    int del;
    while (tonenizer.hasMoreTokens()) {
      next = tonenizer.nextToken();
      if (baseType == null) {
        init(next);
      } else {
        next = next.trim();
        del = next.indexOf("=");
        if (del != -1) {
          String parameter = next.substring(0, del);
          String pValue = next.substring(del + 1);
          if (pValue.startsWith("\"") && pValue.endsWith("\"")) {
            pValue = pValue.substring(1, pValue.length() - 1);
          }
//          System.out.println("Parameter '" + parameter + "' pValue '" + pValue + "'");
          parameters.put(parameter, pValue);
        } else {
          throw new IllegalArgumentException("Incorrect value: " + value + "'");
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
      list.set(next, (String) parameters.get(next));
    }

    return new ContentType(getPrimaryType(), getSubType(), list);
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer(baseType);
    Enumeration en = parameters.keys();
    String current;

    while (en.hasMoreElements()) {
      current = (String) en.nextElement();
      buffer.append(DELIMITER + " ");
      buffer.append(current + "=");
      buffer.append("\"" + (String) parameters.get(current) + "\"");
    }
//    System.out.println("This is toString() '" + buffer.toString() + "'");
    return buffer.toString();
  }
  //  public static void main(String[] args) throws Exception {
//    String s = "multipart/related; boundary=----------MULTIPART_BOUNDARY_f13080cc951----------; type=text/xml";
////    String s = "multipart/related";
//    ContentTypeInner inner = new ContentTypeInner(s);
//    System.out.println(inner.getPrimaryType());
//    System.out.println(inner.getSubType());
//    System.out.println(inner.getBaseType());
//    System.out.println(inner.parameters);
//
//
//    System.out.println("This is the ct '" + ct.toString() + "'");
//  }
}