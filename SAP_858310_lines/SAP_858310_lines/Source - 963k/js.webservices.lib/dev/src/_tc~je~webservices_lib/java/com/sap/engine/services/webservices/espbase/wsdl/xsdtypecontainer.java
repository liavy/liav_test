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
import java.util.List;

import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Element;

import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLException;

/**
 * Instances of this class act as containers of XML Schema, stored as DOMSource objects. 
 * Each Definitions object contains an instance of this class into which the definitions
 * schemas are stored.
 * Instances of this class cannot have children.
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-11-18
 */
public class XSDTypeContainer extends Base {
  
  private List schemas; 
  private URIResolver resolver;
  
  public XSDTypeContainer() throws WSDLException {
    super(Base.XSD_TYPECONTAINER_ID, Base.XSD_TYPECONTAINER_NAME, null);
    this.schemas = new ArrayList(); 
  }
  /**
   * @return URIResolver instance, or null if URIResolver is not set.
   */
  public URIResolver getURIResolver() {
    return resolver;
  }
  /**
   * Sets uri resolvers.
   */
  public void setURIResolver(URIResolver resolver) {
    this.resolver = resolver;
  }
	public void appendChild(Base child) throws WSDLException {
    appendChild(child, Base.NONE_ID);
	}
  
  /**
   * Appends <b>schema</b> parameter to the list of DOMSource objects.
   */  
  public void addSchema(DOMSource schema) {
    this.schemas.add(schema);
  }
  /**
   * Appends <b>schemas</b> parameter to the list of DOMSource objects.
   */
  public void addSchemas(DOMSource[] schemas) {
    for (int i = 0; i < schemas.length; i++) {
      this.addSchema(schemas[i]);
    }
  }  

  /**
   * @return List of javax.xml.transform.dom.DOMSource objects.
   */
  public List getSchemas() {
    return schemas;
  }
  
	protected void toStringAdditionals(StringBuffer buffer) {
    buffer.append("SchemaNumber=" + schemas.size()).append(", schemas: ");
    DOMSource s;
    for (int i = 0; i < schemas.size(); i++) {
      s = (DOMSource) schemas.get(i);
      buffer.append("systemid=" + s.getSystemId()).append(" schema: " + s.getNode());      
    }
	}
  /**
   * Checks for schema with 'targetNamespace' equal to <code>targetNS</code>.
   */
  public boolean containsSchema(String targetNS) {
    DOMSource tmp;
    for (int i = 0; i < schemas.size(); i++) {
      tmp = (DOMSource) schemas.get(i);
      if (((Element) tmp.getNode()).getAttribute("targetNamespace").equals(targetNS)) {
        return true; 
      }
    }
    return false;
  }
}
