﻿/*
 * Generated by SAP SchemaToJava Generator NW05 on Wed Oct 18 15:02:39 EEST 2006
 * Copyright (c) 2002 by SAP Labs Sofia AG.
 * url: http://www.saplabs.bg
 * All rights reserved.
 */
package com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext;

/**
 * Schema complexType Java representation.
 * Represents type {http://www.sap.com/webas/710/ws/webservices-j2ee-engine-ext-descriptor}implementationLinkType
 */
public  class ImplementationLinkType extends com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.PropertyListType implements java.io.Serializable {

  private static final long serialVersionUID = 3673870971530338179L;

  /**
   * Equals method implementation.
   */
  public boolean equals(Object object) {
    if (super.equals(object) == false) return false;
    if (object == null) return false;
    if (!(object instanceof ImplementationLinkType)) return false;
    ImplementationLinkType typed = (ImplementationLinkType) object;
    return true;
  }

  /**
   * Hashcode method implementation.
   */
  public int hashCode() {
    int result = super.hashCode();
    return result;
  }
}
