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

import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLException;

/**
 * Instances of this class represent wsdl SOAP binding/operation entity with its specifics.
 * Only Parameter, ExtensionElement and AttachmentsContainer objects are allowed to be attached as children. 
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-11-21
 */
public class SOAPBindingOperation extends AbstractOperation {
  private static final int MASK  =  Base.PARAMETER_ID | Base.EXTENSION_ELEMENT_ID | Base.ATTACHMENTSCONTAINER_ID;
  /**
   * Constant property key, under which 'soapAction' value is bound.
   */  
  public static final String SOAPACTION = "soapAction";
  /**
   * Constant property key, under which 'style' value is bound.
   */  
  public static final String STYLE = "style";
  /**
   * Constant property key, under which 'use' value is bound.
   * WSDL1.1 allows different 'use' values to be specified on input and output of operation, 
   * but in real life, they cannot be different.
   */
  public static final String USE  =  "use";
  /**
   * Constant property key, under which input body 'namespace' value is bound.
   */
  public static final String INPUT_NAMESPACE  =  "input-namespace";
  /**
   * Constant property key, under which output body 'namespace' value is bound.
   */
  public static final String OUTPUT_NAMESPACE  =  "output-namespace";
  /**
   * Constant property key, under which input body 'encodingStyle' value is bound.
   */
  public static final String INPUT_ENCODINGSTYLE  =  "input-encodingStyle";
  /**
   * Constant property key, under which output body 'encodingStyle' value is bound.
   */
  public static final String OUTPUT_ENCODINGSTYLE  =  "output-encodingStyle"; 
  /**
   * Constant property key, under which whitespace separated list of input headers Parameter names is bound.
   */
  public static final String IN_HEADERS  =  "in-headers"; //list of part name, same as body:parts
  /**
   * Constant property key, under which whitespace separated list of output headers Parameter names is bound.
   */
  public static final String OUT_HEADERS  =  "out-headers"; //list of part name, same as body:parts
  /**
   * Constant property key, under which input body 'parts' attribute value is bound.
   */
  public static final String INPUT_PARTS  =  "input-parts";  
  /**
   * Constant property key, under which output body 'parts' attribute value is bound.
   */
  public static final String OUTPUT_PARTS  =  "output-parts";
  /**
   * Constant representing encoded use value.
   */
  public static final String USE_ENCODED = "encoded";
  /**
   * Constant representing literal use value.
   */
  public static final String USE_LITERAL = "literal";
  //used to recorgnized the additional headers elements
  private static final String HEADER  =  "header";
  private static final String IN_HEADER  =  "in-header";
  private static final String OUT_HEADER  =  "out-header";  
  private static final String IN_ATTACH_CONTAINER_NAME  =  "in-attachment-container";
  private static final String OUT_ATTACH_CONTAINER_NAME  =  "out-attachment-container";
  //encoded fault ns property prefix
  private static final String ENCODED_FAULT_NS_PREFIX  =  "encoded-fault-ns-prefix:";

  
  /**
   * Creates instance, specifying its name.
   * 
   * @param name operation name
   * @throws WSDLException
   */
  public SOAPBindingOperation(String name) throws WSDLException {
    super(Base.SOAPBINDING_OPERATION_ID, Base.SOAPBINDING_OPERATION_NAME, null, name);
  }
  
  /**
   * @return ObjectList containg Parameter object. These Parameter objects are created for the 
   *         input header entities, which input headers are not part of the input operation message.
   *         These are the additional input headers which are not declared in the interface wsdl part, 
   *         but in the binding part.
   */  
  public ObjectList getInAdditionalHeaders() {
    ObjectList l = super.getChildren(Base.PARAMETER_ID);
    ObjectList res = new ObjectList();
    for (int i = 0; i < l.getLength(); i++) {
      if (IN_HEADER.equals(l.item(i).getProperty(HEADER))) {
        res.add(l.item(i));  
      }
    }
    return res;
  }
  /**
   * @return ObjectList containg Parameter object. These Parameter objects are created for the 
   *         output header entities, which output headers are not part of the output operation message.
   *         These are the additional output headers which are not declared in the interface wsdl part, 
   *         but in the binding part.
   */
  public ObjectList getOutAdditionalHeaders() {  
    ObjectList l = super.getChildren(Base.PARAMETER_ID);
    ObjectList res = new ObjectList();
    for (int i = 0; i < l.getLength(); i++) {
      if (OUT_HEADER.equals(l.item(i).getProperty(HEADER))) {
        res.add(l.item(i));  
      }
    }
    return res;
  }

	public void appendChild(Base child) throws WSDLException {
    appendChild(child, MASK);
	}
  
  /**
   * Appends <b>p</b> parameter object to the list of additional binding input headers.
   * @see #getInAdditionalHeaders()
   */
  public void appendInAdditionalHeader(Parameter p) throws WSDLException {
    p.setProperty(HEADER, IN_HEADER);
    appendChild(p);
  }

  /**
   * Appends <b>p</b> parameter object to the list of additional binding output headers.
   * @see #getOutAdditionalHeaders()
   */
  public void appendOutAdditionalHeader(Parameter p) throws WSDLException {
    p.setProperty(HEADER, OUT_HEADER);
    appendChild(p);
  }
  
  /**
   * @return Operation object for which this binding operation instance defines concrete binding properties.
   */
  public Operation getReferencedOperation() throws WSDLException {
    Definitions root = (Definitions) getRoot();
    SOAPBinding parent = (SOAPBinding) getParent();
    Interface intf = root.getInterface(parent.getInterface());
    if (intf != null) {
      Operation op = intf.getOperation(this.name);
      if (op == null) {
        throw new WSDLException(WSDLException.MISSING_ENTITY, new Object[]{Base.OPERATION_NAME, this.name});            
      }
      return op;
    } else {
      throw new WSDLException(WSDLException.MISSING_ENTITY, new Object[]{Base.INTERFACE_NAME, parent.getInterface()});
    }
  }
  /**
   * Returns AttachmentsContainer object, which contians 'input' attachments. 
   * If wsdl:input contains no MIME extensios <code>null</code> is returned.
   * It is possible the returned  container to contain no attachments - in this
   * case wsdl:input contains MIME extensions but these extensions do not describe any attachment.
   */
  public AttachmentsContainer getInputAttachmentsContainer() {
    AttachmentsContainer tmp;
    ObjectList aC = getChildren(Base.ATTACHMENTSCONTAINER_ID);
    for (int i = 0; i < aC.getLength(); i++) {
      tmp = (AttachmentsContainer) aC.item(i);
      if (IN_ATTACH_CONTAINER_NAME.equals(tmp.getName())) {
        return tmp;
      }
    }
    return null;
  }
  /**
   * Returns AttachmentsContainer object, which contians 'output' attachments. 
   * If wsdl:output contains no MIME extensions <code>null</code> is returned.
   * It is possible the returned  container to contain no attachments - in this
   * case wsdl:output contains MIME extensions but these extensions do not describe any attachment.
   */
  public AttachmentsContainer getOutputAttachmentsContainer() {
    AttachmentsContainer tmp;
    ObjectList aC = getChildren(Base.ATTACHMENTSCONTAINER_ID);
    for (int i = 0; i < aC.getLength(); i++) {
      tmp = (AttachmentsContainer) aC.item(i);
      if (OUT_ATTACH_CONTAINER_NAME.equals(tmp.getName())) {
        return tmp;
      }
    }
    return null;
  }
  /**
   * First checks whether input attachment container is already added, if so
   * it is returned. If no input attachments container is available one is created, 
   * attached and returned.
   */
  public AttachmentsContainer appendInputAttachmentsContainer() throws WSDLException {
    AttachmentsContainer aC = getInputAttachmentsContainer();
    if (aC == null) {
      aC = new AttachmentsContainer(IN_ATTACH_CONTAINER_NAME);
      appendChild(aC); 
    }
    return aC;
  }
  /**
   * First checks whether output attachment container is already added, if so
   * it is returned. If no output attachments container is available, one is created, 
   * attached and returned.
   */
  public AttachmentsContainer appendOutputAttachmentsContainer() throws WSDLException {
    AttachmentsContainer aC = getOutputAttachmentsContainer();
    if (aC == null) {
      aC = new AttachmentsContainer(OUT_ATTACH_CONTAINER_NAME);
      appendChild(aC); 
    }
    return aC;
  }
  /**
   * Returns the namespace attribute value for spefic fault in case of rpc/encoded operation.
   * Note this method is only relevant for rpc/encoded operations.
   * @param faultName name of the fault as described by the soap:body@name attribute.
   * @return the fault namespace, or null if no fault with <code>faultName</code> actually exists.
   */
  public String getEncodedFaultNS(String faultName) {
    String fProp = ENCODED_FAULT_NS_PREFIX + faultName;
    return getProperty(fProp);        
  }
  /**
   * Sets namespace attribute value for spefic fault in case of rpc/encoded operation.
   * @param faultName name of the fault as described by the soap:body@name attribute.
   * @return previous value of ns for <code>faultName</code>.
   */
  public String setEncodedFaultNS(String faultName, String ns) {
    String fProp = ENCODED_FAULT_NS_PREFIX + faultName;
    return setProperty(fProp, ns);        
  }
  
	protected void toStringAdditionals(StringBuffer buffer) {
    buffer.append("name=" + name);
	}
  
  
}
