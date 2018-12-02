﻿/*
 * Generated by SAP SchemaToJava Generator NW05 on Wed Oct 18 15:02:39 EEST 2006
 * Copyright (c) 2002 by SAP Labs Sofia AG.
 * url: http://www.saplabs.bg
 * All rights reserved.
 */
package com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext;

/**
 * Schema complexType Java representation.
 * Represents type {http://www.sap.com/webas/710/ws/webservices-j2ee-engine-ext-descriptor}binding-data-Type
 */
public  class BindingDataType implements java.io.Serializable {

  private static final long serialVersionUID = 5572790512010561381L;

  // Element field for element {}binding-data-name
  private java.lang.String _f_BindingDataName;
  /**
   * Set method for element {}binding-data-name
   */
  public void setBindingDataName(java.lang.String _BindingDataName) {
    this._f_BindingDataName = _BindingDataName;
  }
  /**
   * Get method for element {}binding-data-name
   */
  public java.lang.String getBindingDataName() {
    return this._f_BindingDataName;
  }

  // Element field for element {}url
  private java.lang.String _f_Url;
  /**
   * Set method for element {}url
   */
  public void setUrl(java.lang.String _Url) {
    this._f_Url = _Url;
  }
  /**
   * Get method for element {}url
   */
  public java.lang.String getUrl() {
    return this._f_Url;
  }

  // Element field for element {}class-name
  private java.lang.String _f_ClassName;
  /**
   * Set method for element {}class-name
   */
  public void setClassName(java.lang.String _ClassName) {
    this._f_ClassName = _ClassName;
  }
  /**
   * Get method for element {}class-name
   */
  public java.lang.String getClassName() {
    return this._f_ClassName;
  }

  // Element field for element {}implementation-link
  private com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.ImplementationLinkType _f_ImplementationLink;
  /**
   * Set method for element {}implementation-link
   */
  public void setImplementationLink(com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.ImplementationLinkType _ImplementationLink) {
    this._f_ImplementationLink = _ImplementationLink;
  }
  /**
   * Get method for element {}implementation-link
   */
  public com.sap.engine.services.webservices.server.deploy.descriptors.ws.ext.ImplementationLinkType getImplementationLink() {
    return this._f_ImplementationLink;
  }

  // Element field for element {}auth-method
  private java.lang.String _f_AuthMethod;
  /**
   * Set method for element {}auth-method
   */
  public void setAuthMethod(java.lang.String _AuthMethod) {
    this._f_AuthMethod = _AuthMethod;
  }
  /**
   * Get method for element {}auth-method
   */
  public java.lang.String getAuthMethod() {
    return this._f_AuthMethod;
  }

  // Element field for element {}transport-guarantee
  private java.lang.String _f_TransportGuarantee;
  /**
   * Set method for element {}transport-guarantee
   */
  public void setTransportGuarantee(java.lang.String _TransportGuarantee) {
    this._f_TransportGuarantee = _TransportGuarantee;
  }
  /**
   * Get method for element {}transport-guarantee
   */
  public java.lang.String getTransportGuarantee() {
    return this._f_TransportGuarantee;
  }

  // Element field for element {}http-method
  private java.lang.String _f_HttpMethod;
  /**
   * Set method for element {}http-method
   */
  public void setHttpMethod(java.lang.String _HttpMethod) {
    this._f_HttpMethod = _HttpMethod;
  }
  /**
   * Get method for element {}http-method
   */
  public java.lang.String getHttpMethod() {
    return this._f_HttpMethod;
  }

  // Element field for element {}role-name
  private java.lang.String _f_RoleName;
  /**
   * Set method for element {}role-name
   */
  public void setRoleName(java.lang.String _RoleName) {
    this._f_RoleName = _RoleName;
  }
  /**
   * Get method for element {}role-name
   */
  public java.lang.String getRoleName() {
    return this._f_RoleName;
  }

  /**
   * Equals method implementation.
   */
  public boolean equals(Object object) {
    if (object == null) return false;
    if (!(object instanceof BindingDataType)) return false;
    BindingDataType typed = (BindingDataType) object;
    if (this._f_BindingDataName != null) {
      if (typed._f_BindingDataName == null) return false;
      if (!this._f_BindingDataName.equals(typed._f_BindingDataName)) return false;
    } else {
      if (typed._f_BindingDataName != null) return false;
    }
    if (this._f_Url != null) {
      if (typed._f_Url == null) return false;
      if (!this._f_Url.equals(typed._f_Url)) return false;
    } else {
      if (typed._f_Url != null) return false;
    }
    if (this._f_ClassName != null) {
      if (typed._f_ClassName == null) return false;
      if (!this._f_ClassName.equals(typed._f_ClassName)) return false;
    } else {
      if (typed._f_ClassName != null) return false;
    }
    if (this._f_ImplementationLink != null) {
      if (typed._f_ImplementationLink == null) return false;
      if (!this._f_ImplementationLink.equals(typed._f_ImplementationLink)) return false;
    } else {
      if (typed._f_ImplementationLink != null) return false;
    }
    if (this._f_AuthMethod != null) {
      if (typed._f_AuthMethod == null) return false;
      if (!this._f_AuthMethod.equals(typed._f_AuthMethod)) return false;
    } else {
      if (typed._f_AuthMethod != null) return false;
    }
    if (this._f_TransportGuarantee != null) {
      if (typed._f_TransportGuarantee == null) return false;
      if (!this._f_TransportGuarantee.equals(typed._f_TransportGuarantee)) return false;
    } else {
      if (typed._f_TransportGuarantee != null) return false;
    }
    if (this._f_HttpMethod != null) {
      if (typed._f_HttpMethod == null) return false;
      if (!this._f_HttpMethod.equals(typed._f_HttpMethod)) return false;
    } else {
      if (typed._f_HttpMethod != null) return false;
    }
    if (this._f_RoleName != null) {
      if (typed._f_RoleName == null) return false;
      if (!this._f_RoleName.equals(typed._f_RoleName)) return false;
    } else {
      if (typed._f_RoleName != null) return false;
    }
    return true;
  }

  /**
   * Hashcode method implementation.
   */
  public int hashCode() {
    int result = 0;
    if (this._f_BindingDataName != null) {
      result+= this._f_BindingDataName.hashCode();
    }
    if (this._f_Url != null) {
      result+= this._f_Url.hashCode();
    }
    if (this._f_ClassName != null) {
      result+= this._f_ClassName.hashCode();
    }
    if (this._f_ImplementationLink != null) {
      result+= this._f_ImplementationLink.hashCode();
    }
    if (this._f_AuthMethod != null) {
      result+= this._f_AuthMethod.hashCode();
    }
    if (this._f_TransportGuarantee != null) {
      result+= this._f_TransportGuarantee.hashCode();
    }
    if (this._f_HttpMethod != null) {
      result+= this._f_HttpMethod.hashCode();
    }
    if (this._f_RoleName != null) {
      result+= this._f_RoleName.hashCode();
    }
    return result;
  }
}