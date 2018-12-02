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

import javax.xml.namespace.QName;

import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLException;


/**
 * Instances of this class are java representatives of wsdl definitions(wsdl1.1) and description(wsdl2.0) entities.
 * To this object as children could be attached Interface, Binding, Service and ExtensionElement objects. 
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-11-18
 */
public class Definitions extends Base {
  private static final int MASK = Base.INTERFACE_ID | Base.SOAPBINDING_ID | Base.HTTPBINDING_ID 
                                  | Base.SERVICE_ID | Base.EXTENSION_ELEMENT_ID | Base.EXTENSION_CONTEXT_ID;
  
  public static final String VERSION  = "version";
  public static final String WSDL11  =  "WSDL1.1";
  public static final String WSDL20  = "WSDL2.0"; 
  public static final String TARGET_NS = "targetNamespace";
           
  public Definitions() throws WSDLException {  
    super(Base.DEFINITIONS_ID, Base.DEFINITIONS_NAME, null);   
    super.appendChild(new XSDTypeContainer(), Base.XSD_TYPECONTAINER_ID);
    setProperty(VERSION, WSDL11);
  }
  
	public void appendChild(Base child) throws WSDLException {
    if (child instanceof Interface) {
      QName qname = ((Interface) child).getName();
      if (getInterface(qname) != null) {    
        String exParam = child.getObjectName() + "[" + qname + "]";
        throw new WSDLException(WSDLException.DUPLICATE_ENTITIES, new Object[]{exParam, getObjectName()});
      }
    }
    if (child instanceof Binding) {
      QName qname = ((Binding) child).getName();
      if (getBinding(qname) != null) {    
        String exParam = child.getObjectName() + "[" + qname + "]";
        throw new WSDLException(WSDLException.DUPLICATE_ENTITIES, new Object[]{exParam, getObjectName()});
      }
    }
    if (child instanceof Service) {
      QName qname = ((Service) child).getName();
      if (getService(qname) != null) {    
        String exParam = child.getObjectName() + "[" + qname + "]";
        throw new WSDLException(WSDLException.DUPLICATE_ENTITIES, new Object[]{exParam, getObjectName()});
      }
    }
    appendChild(child, MASK);
	}
  
  /**
   * Creates and appends an Interface object with qualified name, equal to <code>qname</code> parameter.
   * @param qname interface qualified name
   * @return newly created and attached Interface object.
   */
  public Interface appendInterface(QName qname) throws WSDLException {
    Interface intf = new Interface(qname);
    appendChild(intf);
    return intf;
  }
    
  /**
   * Creates and appends a Serevice object with qualified name, equal to <code>qname</code> parameter.
   * @param qname service qualified name
   * @return newly created and attached Service object.
   */
  public Service appendService(QName qname) throws WSDLException {
    Service service = new Service(qname);
    appendChild(service);
    return service;
  }
  
  /**
   * @return XMLSchema type container of this definitions object. 
   */
  public XSDTypeContainer getXSDTypeContainer() {
    ObjectList list = super.getChildren(Base.XSD_TYPECONTAINER_ID);
    if (list.getLength() <= 0) {
      return null; 
    }
    return (XSDTypeContainer) list.item(0);  
  }
  
  /**
   * @param qname interface qualified name
   * @return Interface object, which qualified name is equal to <b>qname</b> param, or null if such Interface does not exist.
   */
  public Interface getInterface(QName qname) {
    ObjectList list = getChildren(Base.INTERFACE_ID);
    Interface cur;
    for (int i = 0; i < list.getLength(); i++) {
      cur = (Interface) list.item(i);
      if (cur.getName().equals(qname)) {
        return cur;
      }
    }
    return null;
  }
  
  /**
   * @param qname binding qualified name
   * @return Binding object, which qualified name is equal to <b>qname</b> param, or null if such Binding does not exist.
   */  
  public Binding getBinding(QName qname) {
    ObjectList list = getChildren();
    Binding cur;
    for (int i = 0; i < list.getLength(); i++) {
      if (list.item(i) instanceof Binding) {
        cur  = (Binding) list.item(i);
        if (cur.getName().equals(qname)) {
          return cur;
        }        
      }
    }
    return null;
  }
  
  /**
   * @param qname service qualified name
   * @return Service object, which qualified name is equal to <b>qname</b> param, or null if such Service does not exist.
   */  
  public Service getService(QName qname) {
    ObjectList list = getChildren(Base.SERVICE_ID);
    Service cur;
    for (int i = 0; i < list.getLength(); i++) {
      cur  = (Service) list.item(i);
      if (cur.getName().equals(qname)) {
        return cur;
      }        
    }
    return null;    
  }

  /**
   * @return ObjectList object, containing all Service objects, that are attached to this definitions object.
   */    
  public ObjectList getServices() {
    return getChildren(Base.SERVICE_ID);
  }

  /**
   * @param definitionsTargetNS The owner namespace of the parent wsdl definitions.
   * @return List of extension elements defined by the specified wsdl definitions target namespace.  
   */
  public ObjectList getExtensionElements(String definitionsTargetNS) {
    ObjectList extensionElems = new ObjectList();
    ObjectList children = getChildren();
    for(int i = 0; i < children.getLength(); i++) {
      Base childBase = children.item(i); 
      if (childBase.getType() == EXTENSION_ELEMENT_ID && ((ExtensionElement)childBase).getOwnerNamespace().equals(definitionsTargetNS)) {
        extensionElems.add(childBase);      
      }
    }
    return(extensionElems);
  }
  
  /**
   * @return ObjectList object, containing all Binding objects, that are attached to this definitions object.
   */    
  public ObjectList getBindings() {
    ObjectList l = getChildren();
    ObjectList list = new ObjectList();
    for (int i = 0; i < l.getLength(); i++) {
      if (l.item(i) instanceof Binding) {
        list.add(l.item(i));
      }
    }
    return list;
  }
  /**
   * Returns the extension context for message <code>msgQName</code>.
   * If such message is not available null is returned.
   * @param msgQName
   * @return
   */
  public ExtensionContext getMessageContext(QName msgQName) {
    String targetCtxName = "message-ext-context:" + msgQName; //this is how the name is constructed in WSDL11Loader
    
    ObjectList msgCtxs = getChildren(EXTENSION_CONTEXT_ID);
    for (int i = 0; i < msgCtxs.getLength(); i++) {
      ExtensionContext ctx = (ExtensionContext) msgCtxs.item(i);
      String ctxName = ctx.getName();
      if (targetCtxName.equals(ctxName)) {
        return ctx;
      }
    }
    return null;
  }
  /**
   * @return ObjectList object, containing all Interface objects, that are attached to this definitions object.
   */    
  public ObjectList getInterfaces() {
    return getChildren(Base.INTERFACE_ID);
  }
 
	protected void toStringAdditionals(StringBuffer buffer) {
	}
}
