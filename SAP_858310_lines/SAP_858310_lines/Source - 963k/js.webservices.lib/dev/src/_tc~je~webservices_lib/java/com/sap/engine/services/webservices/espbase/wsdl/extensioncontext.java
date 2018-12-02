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
 * This class is used as a container for certain extension artifacts.
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-11-19 
 */
public class ExtensionContext extends Base {
  
  private String name;
  
  public ExtensionContext(String name) throws WSDLException {
    super(Base.EXTENSION_CONTEXT_ID, Base.EXTENSION_CONTEXT_NAME, null);
    this.name = name;  
  }

	public void appendChild(Base child) throws WSDLException {
    appendChild(child, Base.EXTENSION_CONTEXT_ID | Base.EXTENSION_ELEMENT_ID);
	}

	public String getName() {
		return name;
	}

	public void setName(String string) {
		name = string;
	}

	protected void toStringAdditionals(StringBuffer buffer) {
    buffer.append("name=" + name);
	}

}
