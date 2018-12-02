/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.wsdl;

import java.util.ArrayList;

/**
 * This class represents a list of Base objects.
 * It is used as a helper class in the dealing with the objects from this API. 
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-11-18
 */
public class ObjectList {
  protected ArrayList items = new ArrayList();
  
  ObjectList() {
  }
  
  /**
   * @param pos index in the list which Base object is to be returned. First item in the list, has index of 0. 
   * @return Base object on specified <b>pos</b> index.
   */  
  public Base item(int pos) {
    return (Base) items.get(pos);
  }
  
  /**
   * @return number of entites in the list.
   */
  public int getLength() {
    return items.size();
  }
  
  void add(Base b) {
    items.add(b);
  }
  
  Base remove(Base b) {
    return (Base) (items.remove(b) ? b : null);
  }
}
