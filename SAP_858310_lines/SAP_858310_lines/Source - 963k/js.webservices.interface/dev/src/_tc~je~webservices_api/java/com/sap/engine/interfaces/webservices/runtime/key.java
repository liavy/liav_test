/**
 * Copyright (c) 2002 by SAP Labs Bulgaria.,
 * All rights reserved.
 */
package com.sap.engine.interfaces.webservices.runtime;

import java.io.Serializable;

/**
 *   This class represents name-value pair which is used
 * for identification.
 *
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

final public class Key implements Serializable {

  private String name = null;
  private String value = null;

  public Key() {

  }

  public Key(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public String toString() {
    return "Key name:'" + this.name + "' key value:'" + this.value + "'";
  }

  public int hashCode() {
    if (this.name == null || this.value == null) return 0;
    return this.name.hashCode() * this.value.hashCode();
  }

  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (!(obj instanceof Key)) return false;
    Key objKey = (Key)obj;
    return (this.name.equals(objKey.getName()) && this.value.equals(objKey.getValue()));
  }

}