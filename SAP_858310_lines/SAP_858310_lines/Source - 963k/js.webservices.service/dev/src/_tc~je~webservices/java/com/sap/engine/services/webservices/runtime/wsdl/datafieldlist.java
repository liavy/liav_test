package com.sap.engine.services.webservices.runtime.wsdl;

import com.sap.engine.lib.descriptors.ws04vi.TypeState;

import java.util.ArrayList;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */

public class DataFieldList {
  private ArrayList list;
  private TypeState typeState;
  boolean isSimpleContent;
  boolean isUnordered; //if true the complexType will be generated with <all>, otherwise with <sequence>

  public DataFieldList(TypeState typeState) {
    this.typeState = typeState;
    list = new ArrayList();
  }

  public TypeState getTypeState() {
    return this.typeState;
  }

  public int size() {
    return list.size();
  }

  public int length() {
    return list.size();
  }

  public DataField item(int index) {
    return (DataField) list.get(index);
  }

  public void add(DataField item) {
    list.add(item);
  }

  public void add(int index, DataField item) {
    list.add(index, item);
  }

  public void clear() {
    list.clear();
  }

  public Object remove(int i) {
    return list.remove(i);
  }

  public String toString() {
    String ret = "DataFieldList: " + typeState + "\n";
    for (int i = 0; i < size(); i++) {
      ret += "  " + item(i) + "\n";
    }
    return ret;
  }

  public void append(DataFieldList appendList) {
    for (int i = 0; i < appendList.length(); i++) {
      this.list.add(appendList.item(i));
    }
  }

//  public Iterator iterator() {
//    return list.iterator();
//  }
//
//  public boolean countains(DataField df) {
//    for (int i = 0; i < list.size(); i++) {
//      if (((DataField) list.get(i)).getFieldName().equals(df.getFieldName())) {
//        return true;
//      }
//    }
//    return false;
//  }

}