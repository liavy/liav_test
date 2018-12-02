/*
 * Copyright (c) 2005 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.client;

import java.util.HashMap;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Element;

import com.sap.engine.lib.xml.dom.DOM;

public class NamespaceContextResolver implements  NamespaceContext {
  
  private Element scope;
  private HashMap<String,String> defaultMap = new HashMap<String,String>();  
  
  public NamespaceContextResolver() {    
  }
  
  public void setDefaultPrefix(String prefix, String namespace) {
    this.defaultMap.put(prefix,namespace);
  }
  
  public void setScope(Element namespaceScope) {
    this.scope = namespaceScope;
  }    

  /**
   * @param prefix
   * @return
   */
  public String getNamespaceURI(String prefix) {
    String temp = DOM.prefixToURI(prefix,this.scope);
    if (temp == null) {
      return this.defaultMap.get(prefix);
    }
    return temp;
  }

  /**
   * @param namespaceURI
   * @return
   */
  public String getPrefix(String namespaceURI) {
    String temp = DOM.getPrefixForNS(this.scope,namespaceURI);
    return temp;
  }

  /**
   * @param namespaceURI
   * @return
   */
  public Iterator getPrefixes(String namespaceURI) {    
    return DOM.getNamespaceMappingsInScope(this.scope).keySet().iterator();
  }  
}
