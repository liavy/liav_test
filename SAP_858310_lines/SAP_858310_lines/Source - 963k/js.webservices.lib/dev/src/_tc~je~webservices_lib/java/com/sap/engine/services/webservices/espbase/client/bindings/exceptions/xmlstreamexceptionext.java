/*
 * Copyright (c) 2007 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.client.bindings.exceptions;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2007, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, Jan 17, 2007
 */
public class XMLStreamExceptionExt extends XMLStreamException {

  public XMLStreamExceptionExt() {
    super();
  }

  public XMLStreamExceptionExt(String arg0, Location arg1, Throwable arg2) {
    super(arg0, arg1, arg2);
    initCause(arg2);
  }

  public XMLStreamExceptionExt(String arg0, Location arg1) {
    super(arg0, arg1);
  }

  public XMLStreamExceptionExt(String arg0, Throwable arg1) {
    super(arg0, arg1);
    initCause(arg1);
  }

  public XMLStreamExceptionExt(String arg0) {
    super(arg0);
  }

  public XMLStreamExceptionExt(Throwable arg0) {
    super(arg0);
    initCause(arg0);
  }

}
