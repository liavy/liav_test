package com.sap.engine.services.webservices.runtime.definition;

import java.util.Hashtable;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */

public class PropertyDescriptorInterfaceImpl implements com.sap.engine.interfaces.webservices.runtime.PropertyDescriptor {

  private String propertyName;
  private String value;
  private String simpleContent;
  private Hashtable internals;

  public PropertyDescriptorInterfaceImpl(String propertyName, String value) {
    this.propertyName = propertyName;
    this.value = value;
  }

  public PropertyDescriptorInterfaceImpl() {
  }

  public PropertyDescriptorInterfaceImpl(String propertyName, Hashtable internals) {
    this.propertyName = propertyName;
    this.internals = internals;
  }

  public String getPropertyDescriptorName() {
    return this.propertyName;
  }

  public boolean hasValueAttrib() {
    return (this.value != null);
  }

  public String getValue() {
    return this.value;
  }

  public boolean hasSimpleContent() {
    return (this.simpleContent != null);
  }

  public String getSimpleContent() {
    return this.simpleContent;
  }

  public com.sap.engine.interfaces.webservices.runtime.PropertyDescriptor getInternalDescriptor(String propertyName) {
    if (this.internals != null) {
      return (com.sap.engine.interfaces.webservices.runtime.PropertyDescriptor) internals.get(propertyName);
    }

    return null;
  }

  public Hashtable getInternalDescriptors() {
    return this.internals;
  }

  public void setSimpleContent(String simpleContent) {
    this.simpleContent = simpleContent;
  }

  public void addInternal(com.sap.engine.interfaces.webservices.runtime.PropertyDescriptor internal) {
    if (this.internals == null) {
      this.internals = new Hashtable();
    }
    this.internals.put(internal.getPropertyDescriptorName(), internal);
  }

  public void setPropertyName(String propertyName) {
    this.propertyName = propertyName;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void setInternals(Hashtable internals) {
    this.internals = internals;
  }

  public String toString() {
    return "[->Name: '" + this.propertyName + "' value: '" + this.value + "' simpleContent: '" + this.simpleContent + "' internals: " + this.internals;
  }
}