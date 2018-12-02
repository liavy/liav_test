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
 * Instances of this class are java representatives or wsdl portType(interface)/operation entites.
 * Objects of this class can be attached as children only to Interface objects
 * Only Parameter and ExtensionElement objects are allowed to be attached as children to instances of that class. 
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-11-18
 */
public class Operation extends AbstractOperation {
  /**
   * Property indicating the MessageExchangePattern valid for this operation. 
   */
  public static final String MEP  =  "mep";   
  /**
   * MEP value denoting request-response exchange pattern.
   */
  public static final String INOUT_MEP  =  "in-out";
  /**
   * MEP value denoting one-way operation.
   */
  public static final String IN_MEP  =  "in";
    
  private static final int MASK  =  Base.PARAMETER_ID | Base.EXTENSION_ELEMENT_ID | Base.EXTENSION_CONTEXT_ID;
  
  /**
   * Creates Operation object with spefic operation name.
   * @param name operation name
   * @throws WSDLException
   */
  public Operation(String name) throws WSDLException {
    super(Base.OPERATION_ID, Base.OPERATION_NAME, null, name);
  }
  
  /**
   * Creates Operation object without a name
   * @throws WSDLException
   */
  public Operation() throws WSDLException {
    this(null);
  }
  
	public void appendChild(Base child) throws WSDLException {
		 appendChild(child, MASK);
	}
  
  /**
   * Creates and appends Parameter object to child list of this object
   * @param name name of the Parameter object
   * @param type type of the Parameter object e.g Parameter.IN, Parameter.OUT, ...
   * @return newly created Parameter object.
   */
  public Parameter appendParameter(String name, int type) throws WSDLException {
    Parameter p = new Parameter(name, type);
    appendChild(p);
    return p;
  }
  
	protected void toStringAdditionals(StringBuffer buffer) {
    buffer.append("Name=" + name);
	}
  
  /**
   * Returns a list of Parameter object, which types (IN, OUT, ...) are matched by the <b>typeMask</b> param.
   * @param typeMask mask representing parameter types to be included in the list. 
   *        Example: If Parameters of type Parameter.IN and Parameter.OUT need to be return the mask is constructed as
   *                 Parameter.IN | Parameter.OUT.  
   * @return ObjectList containing Parameter objects.
   */
  public ObjectList getParameters(int typeMask) {
    ObjectList list = new ObjectList();
    ObjectList params = getChildren(PARAMETER_ID);
    for (int i = 0; i < params.getLength(); i++) {
      if ((((Parameter) params.item(i)).getParamType() & typeMask) != 0) {
        list.add(params.item(i)); 
      }
    }
    return list;
  }
}
