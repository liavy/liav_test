/*
 * Copyright (c) 2003 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.client.bindings;


public class SerializableDynamicServiceImpl extends DynamicServiceImpl implements java.io.Serializable {

  public void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    writeObjectX(out);
  }
  
  public void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    readObjectX(in);
  }
  
}