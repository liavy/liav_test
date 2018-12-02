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
package com.sap.engine.services.webservices.jaxrpc.wsdl2java;

/**
 * List that contains abstract protocol chain. It is used to pass and work with protocol chains.
 * If is used in ClientTransportBinding interface to pass protocols for calling. 
 *
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class ProtocolList extends java.util.ArrayList {
  
  /**
   * Adds Abstract Protocol to list. 
   * Throws IllegalArgumentException if not an instance of Abstract Protocol passed.
   */ 
  public boolean add(Object object) {
    if (object instanceof AbstractProtocol) {
      super.add(object);
      return true;
    } else {
      throw new IllegalArgumentException(" Only Abstract Protocols can be added in protocol list !");
    }
  }
  
  /**
   * Returns Abstract Protocol by it's index.
   */ 
  public AbstractProtocol getProtocol(int i) {
    return (AbstractProtocol) super.get(i);    
  }
  
  /**
   * Returns protocol by it's name from the chain.
   * If null passed it returns the first not named protocol (name == null).
   * If not found returns null.
   */ 
  public AbstractProtocol getProtocol(String protocolName) {
    for (int i=0; i<super.size(); i++) {
      AbstractProtocol protocol = (AbstractProtocol) super.get(i);
      if (protocol.getName()!= null) {
        if (protocol.getName().equals(protocolName)) {
          return protocol;
        }        
      }
      if (protocol.getName() == protocolName) { //$JL-STRING$
        return protocol;
      }
    }
    return null;
  }
  
  /**
   * Call's all protocols in Protocol List in order aligned.
   * Returns false if some protocol interupted processing.        
   */ 
  public boolean handleRequest(AbstractMessage message, PropertyContext context) throws ClientProtocolException {
    for (int i=0; i<super.size(); i++) {
      AbstractProtocol protocol = (AbstractProtocol) super.get(i);
      if (protocol.handleRequest(message,context) == false) {
        return false;
      }
    }
    return true;
  }
  
  /**
   * Called when response is recieved from service.
   * Return false if some protocol interrupted processing. 
   */ 
  public boolean handleResponse(AbstractMessage message, PropertyContext context) throws ClientProtocolException {
    for (int i=super.size()-1; i>=0; i--) {
      AbstractProtocol protocol = (AbstractProtocol) super.get(i);
      if (protocol.handleResponse(message,context) == false) {
        return false;
      }      
    }
    return true;
  }
  
  /**
   * Called when fault is recieved from service.
   * Return false if some protocol blocked others.   
   */
  public boolean handleFault(AbstractMessage message, PropertyContext context) throws ClientProtocolException {
    for (int i = (super.size()-1); i>=0; i--) {
      AbstractProtocol protocol = (AbstractProtocol) super.get(i);            
      if (protocol.handleFault(message,context) == false) {
        return false;
      }
    }       
    return true;
  }

}
