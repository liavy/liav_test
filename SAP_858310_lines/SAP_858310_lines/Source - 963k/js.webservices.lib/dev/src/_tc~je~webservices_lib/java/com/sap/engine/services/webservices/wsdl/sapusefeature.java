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
package com.sap.engine.services.webservices.wsdl;

import com.sap.engine.lib.xml.parser.handlers.SimpleAttr;
import com.sap.engine.lib.xml.util.QName;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * Class representing SAP UseFeature WSDL extension.
 *
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class SAPUseFeature extends WSDLNode {//$JL-EQUALS$

  protected QName feature;
  
  public SAPUseFeature() {
    super();
  }
  
  public SAPUseFeature(WSDLNode parent) {
    super(parent);
  }

  public String getFeature() {
    if (this.feature == null) {
      return null;
    } else {
      return this.feature.getLocalName();
    }
  }

  public void setFeature(String feature) {
    this.feature = new QName(null,feature);
  }

  public QName getFeatureQName() {
    return this.feature;
  }

  public void setFeatureQName(QName qname) {
    this.feature = qname;
  }

  /**
   * Compares two SAPUsefeatures.
   */
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SAPUseFeature)) {
      return false;
    }
    SAPUseFeature compare = (SAPUseFeature) obj;
    return this.feature.equals(compare.feature);
  }
  
  public int hashCode() {
    return super.hashCode();
  }
  /**
   * Loads name property form List of attributes. (Used from WSDL Handler will be deprecated)
   */
  public void loadAttributes(SimpleAttr[] attr, int attrCount) throws WSDLException {
    String feature = SimpleAttr.getAttribute("feature", attr, attrCount);

    if (feature == null) {
      throw new WSDLException(" No 'feature' attribute !");
    } else {
      this.feature = new QName(null,feature);
    }
  }

  /**
   * Loads attributes from dom element.
   */
  public void loadAttributes(Element element) throws WSDLException {
    Attr attribute = element.getAttributeNode("feature");    
    if (attribute == null) {
      throw new WSDLException(" No 'feature' attribute found !");
    } else {
      this.feature = super.getQName(attribute.getValue(), element);
      if (this.feature.getURI() == null || this.feature.getURI().length()==0) {
        this.feature.setURI(((WSDLDefinitions) this.getDocument()).getTargetNamespace());
      }
    }
  }
  
  
}
