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
package com.sap.engine.services.webservices.espbase.wsdl;

import javax.xml.namespace.QName;

import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLException;

/**
 * This abstract class provides methods which are similar to operations from interface and binding level.
 * Thus the specific interface and binding operation classes extend this class to utilize its methods.
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2005-11-23
 */
public abstract class AbstractOperation extends Base {
  /**
   * Name of the ExtensionContext object that holds the 'input' channel extension elements and attributes. 
   */
  private static final String INPUTCHANNEL_EXT_CONTEXT_NAME  = "input-channel-extension-context";
  /**
   * Name of the ExtensionContext object that holds the 'output' channel extension elements and attributes. 
   */
  private static final String OUTPUTCHANNEL_EXT_CONTEXT_NAME  = "output-channel-extension-context";
  /**
   * Name of the ExtensionContext object that holds 'fault' channel extension elements and attributes. 
   */
  private static final String FAULTCHANNEL_EXT_CONTEXT_NAME  = "fault-channel-extension-context";
  /**
   * This property and its value determines the 'fault' channel which certain ExtensionContext represents. 
   */
  private static final String FAULTCHANNEL_NAME_PROP  = "fault-channel-name";

  protected String name;  
 
  public AbstractOperation(int type, String objectName, Base parent, String name) throws WSDLException {
    super(type, objectName, parent);
    this.name = name;
  }
  
  /**
   * @return operation name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets operation name
   * @param name operation name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return ObjectList containing ExtensionElement objects, which represent the extension elements in the operation 'input' channel.
   *         If there are no extension elements at 'input' channel an empty ObjectList is returned.
   */
  public ObjectList getInputChannelExtensionElements() throws WSDLException {
    ExtensionContext extCtx = getInputExtensionContext();
    return extCtx.getChildren();
  }
  
  /**
   * @param attrQName extension attribute qname
   * @return value of 'input' channel extension attribute with qname <code>attrQName</code>.
   *         If such attribute does not exist, null is returned. 
   */
  public String getInputChannelExtensionAttributeValue(QName attrQName) throws WSDLException {
    ExtensionContext extCtx = getInputExtensionContext();
    return extCtx.getExtensionAttr(attrQName); 
  }
  /**
   * @return ObjectList containing ExtensionElement objects, which represent the extension elements in the operation 'output' channel.
   *         If there are no extension elements at 'output' channel an empty ObjectList is returned.
   */
  public ObjectList getOutputChannelExtensionElements() throws WSDLException {
    ExtensionContext extCtx = getOutputExtensionContext();
    return extCtx.getChildren(); 
  }
  /**
   * @param attrQName extension attribute qname
   * @return value of 'output' channel extension attribute with qname <code>attrQName</code>.
   *         If such attribute does not exist, null is returned. 
   */
  public String getOutputChannelExtensionAttributeValue(QName attrQName) throws WSDLException {
    ExtensionContext extCtx = getOutputExtensionContext();
    return extCtx.getExtensionAttr(attrQName); 
  }
  /**
   * @param faultName the value of '<binding>/<fault>@name' attribute.
   * @return ObjectList containing ExtensionElement objects, which represent the extension elements in the operation 'fault' channel with fault name <code>faultName</code>.
   *         If there are no extension elements for <code>faultName</code> an empty ObjectList is returned.
   */
  public ObjectList getFaultChannelExtensionElements(String faultName) throws WSDLException {
    ExtensionContext extCtx = getFaultExtensionContext(faultName);
    return extCtx.getChildren(); 
  }
  /**
   * @param faultName name of the fault - the value of <operation>/<fault>@name attribute.
   * @param attrQName extension attribute qname
   * @return value of 'fault' channel extension attribute with qname <code>attrQName</code>.
   *         If such attribute does not exist, null is returned. 
   */
  public String getFaultChannelExtensionAttributeValue(String faultName, QName attrQName) throws WSDLException {
    ExtensionContext extCtx = getFaultExtensionContext(faultName);
    return extCtx.getExtensionAttr(attrQName); 
  }
  /**
   * @param faultName name of the fault - the value of <operation>/<fault>@name attribute.
   * @param extEl ExtensionElement object, which wraps the 'real' extension element.
   * Appends <code>extEl</code> to the list of operation children, as extension element for fault <code>faultName</code>. The <code>faultName</code> is
   * the value of '<binding>/<fault>@name' attribute.
   */
  public void appendFaultExtensionElement(String faultName, ExtensionElement extEl) throws WSDLException {
    ExtensionContext extCtx = getFaultExtensionContext(faultName);
    extCtx.appendChild(extEl);
  }
  /**
   * Appends extension attribute with qname <code>attrQName</code> and value <code>attrValue</code>, to 
   * the extension attributes list of 'fault' channel with name <code>faultName</code>.
   */  
  public void appendFaultExtensionAttribute(String faultName, QName attrQName, String attrValue) throws WSDLException {
    ExtensionContext extCtx = getFaultExtensionContext(faultName);
    extCtx.setExtensionAttr(attrQName, attrValue);    
  }
  /**
   * Appends <code>extEl</code> to the internal list of children and marks it as extension element for operation's 'input' channel 
   */
  public void appendInputChannelExtensionElement(ExtensionElement extEl) throws WSDLException {
    ExtensionContext extCtx = getInputExtensionContext();
    extCtx.appendChild(extEl);
  }
  /**
   * Appends extension attribute with qname <code>attrQName</code> and value <code>attrValue</code> to 
   * the extension attributes list of operation's 'input' channel.
   */  
  public void appendInputChannelExtensionAttribute(QName attrQName, String attrValue) throws WSDLException {
    ExtensionContext extCtx = getInputExtensionContext();
    extCtx.setExtensionAttr(attrQName, attrValue);    
  }
  /**
   * Appends <code>extEl</code> to the internal list of children and marks it as extension element for operation's 'output' channel 
   */
  public void appendOutputChannelExtensionElement(ExtensionElement extEl) throws WSDLException {
    ExtensionContext extCtx = getOutputExtensionContext();
    extCtx.appendChild(extEl);
  }
  /**
   * Appends extension attribute with qname <code>attrQName</code> and value <code>attrValue</code> to 
   * the extension attributes list of operation's 'output' channel.
   */  
  public void appendOutputChannelExtensionAttribute(QName attrQName, String attrValue) throws WSDLException {
    ExtensionContext extCtx = getOutputExtensionContext();
    extCtx.setExtensionAttr(attrQName, attrValue);    
  }
  /**
   * Returns the 'input' extension context. If none is availbe, a new one is created, attached and returned.
   * @return
   * @throws WSDLException
   */
  public ExtensionContext getInputExtensionContext() throws WSDLException {
    ExtensionContext extCtx = getExtensionContext(INPUTCHANNEL_EXT_CONTEXT_NAME, null);
    if (extCtx == null) {
      extCtx = new ExtensionContext(INPUTCHANNEL_EXT_CONTEXT_NAME);
      this.appendChild(extCtx);
    }
    return extCtx;
  }
  
  /**
   * Returns the 'output' extension context. If none is availbe, a new one is created, attached and returned.
   * @return
   * @throws WSDLException
   */
  public ExtensionContext getOutputExtensionContext() throws WSDLException {
    ExtensionContext extCtx = getExtensionContext(OUTPUTCHANNEL_EXT_CONTEXT_NAME, null);
    if (extCtx == null) {
      extCtx = new ExtensionContext(OUTPUTCHANNEL_EXT_CONTEXT_NAME);
      this.appendChild(extCtx);
    }
    return extCtx;
  }
  /**
   * Returns the 'fault' extension context, which corresponds to the fault with name <code>faultName</code>
   * @param faultName
   * @return
   * @throws WSDLException
   */
  public ExtensionContext getFaultExtensionContext(String faultName) throws WSDLException {
    ExtensionContext extCtx = getExtensionContext(FAULTCHANNEL_EXT_CONTEXT_NAME, faultName);
    if (extCtx == null) {
      extCtx = new ExtensionContext(FAULTCHANNEL_EXT_CONTEXT_NAME);
      extCtx.setProperty(FAULTCHANNEL_NAME_PROP, faultName);
      this.appendChild(extCtx);
    } 
    return extCtx;
  }

  protected void appendChild(Base child, int mask) throws WSDLException {
    super.appendChild(child, mask | EXTENSION_CONTEXT_ID); //allow extension context to be added as child
  }
  /**
   * Returns ExtensionContext instance from this object child list, where ExtensionContext name equals to <code>contextName</code>. In case the <code>contextName</code>
   * equals FAULTCHANNEL_EXT_CONTEXT_NAME, the second parameter <code>faultName</code> is taken in cosideration and the fault contexts are checked for property with
   * name FAULTCHANNEL_NAME_PROP and value equals to <code>faultName</code>.
   * 
   * @return ExtensionContext instance if found or null.
   */  
  private ExtensionContext getExtensionContext(String contextName, String faultName) {
    ObjectList extCtxs = this.getChildren(Base.EXTENSION_CONTEXT_ID);
    ExtensionContext extCtx;
    for (int i = 0; i < extCtxs.getLength(); i++) {
      extCtx = (ExtensionContext) extCtxs.item(i); 
      if (contextName.equals(extCtx.getName())) {
        if (FAULTCHANNEL_EXT_CONTEXT_NAME.equals(contextName)) {
          String cfName = extCtx.getProperty(FAULTCHANNEL_NAME_PROP);
          if (cfName.equals(faultName)) {
            return extCtx;
          }
        } else { //this is not a fault context
          return extCtx;
        }
      }
    }
    return null;
  }
}
